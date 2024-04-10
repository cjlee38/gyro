package io.cjlee.gyro;

import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.scheduler.Scheduler;
import io.cjlee.gyro.task.Task;
import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

public class LeakyBucketThrottler extends AbstractThrottler implements Throttler {
    private final int leak;
    private final long streamRate;

    public LeakyBucketThrottler(int leak,
                                Duration interval,
                                ExecutorService worker,
                                TaskQueue queue,
                                Scheduler scheduler) {
        super(interval, worker, queue, scheduler);
        this.leak = leak;
        this.streamRate = interval.toNanos() / leak;
    }

    @Override
    protected void executeSubmitted(long started, Duration timeout) {
        for (int processed = 0; processed < leak && !timeout.isNegative(); processed++) {
            Task task = queue.poll(timeout);
            long elapsed = ticker.elapsed(started);

            if (task == null) {
                return;
            }

            // Sleep more if a task polled faster than expected.
            if (streamRate * processed > elapsed) {
                ThreadUtils.nanoSleep(Duration.ofNanos(streamRate - elapsed));
            }
            timeout = timeout.minusNanos(ticker.elapsed(started));
            worker.execute(task);
        }
    }
}
