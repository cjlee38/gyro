package io.cjlee.gyro;

import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.scheduler.Scheduler;
import io.cjlee.gyro.task.Task;
import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenBucketThrottler extends AbstractThrottler implements Throttler {

    private static final Logger log = LoggerFactory.getLogger(TokenBucketThrottler.class);

    private final int capacity;
    private final int replenishAmount;
    private final Duration replenishDelay;
    private final AtomicInteger token; // AtomicLong can be a bottleneck in case of contention.

    public TokenBucketThrottler(int capacity,
                                int replenishAmount,
                                Duration replenishDelay,
                                ExecutorService workers,
                                TaskQueue queue,
                                Scheduler scheduler) {
        super(replenishDelay, workers, queue, scheduler);
        this.capacity = capacity;
        this.replenishAmount = replenishAmount;
        this.replenishDelay = replenishDelay;
        this.token = new AtomicInteger(capacity);
    }

    @Override
    protected void executeSubmitted(long started, Duration timeout) {
        replenishToken();
        int concurrency = concurrency();

        for (int processed = 0; processed < concurrency && !timeout.isNegative(); processed++) {
            Task task = queue.poll(timeout);
            long elapsed = ticker.elapsed(started);

            if (task == null) {
                return;
            }

            token.decrementAndGet();
            long streamRate = streamRate(concurrency, processed);

            // Sleep more if task polled faster than expected.
            if (streamRate > elapsed) {
                ThreadUtils.nanoSleep(Duration.ofNanos(streamRate - elapsed));
            }
            timeout = timeout.minusNanos(ticker.elapsed(started));
            worker.execute(task);
        }
    }

    private void replenishToken() {
        token.updateAndGet(it -> Math.min(capacity, it + replenishAmount));
    }

    private int concurrency() {
        return Math.min(capacity, token.get());
    }

    private long streamRate(int concurrency, int processed) {
        return (concurrency == 0 ? replenishDelay : replenishDelay.dividedBy(concurrency)).toNanos() * processed;
    }
}
