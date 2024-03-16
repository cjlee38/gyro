package io.cjlee.sandevistan;

import io.cjlee.sandevistan.ticker.NativeTicker;
import io.cjlee.sandevistan.ticker.Ticker;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// [throttler] delay task --submit-> [queue] <- peek & schedule next task by `whenToStart`
public class TokenBucketThrottler implements Throttler {

    private static final Logger logger = LoggerFactory.getLogger(TokenBucketThrottler.class);

    private final long capacity;
    private final long replenishAmount;
    private final Duration replenishDelay;
    private final ExecutorService workers = Executors.newCachedThreadPool(); // TODO : constructor

    private final Ticker ticker = new NativeTicker();
    private final ScheduledExecutorService supplementary = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService peeker = Executors.newSingleThreadScheduledExecutor();
    private final LinkedBlockingQueue<DelayedTask> queue = new LinkedBlockingQueue<>();
    private final AtomicLong token;

    private final AtomicBoolean running = new AtomicBoolean(); // TODO : current code is just for simplicity. Analyze and make it efficient.

    public TokenBucketThrottler(long capacity, long replenishAmount, Duration replenishDelay) {
        this.capacity = capacity;
        this.replenishAmount = replenishAmount;
        this.replenishDelay = replenishDelay;
        this.token = new AtomicLong(replenishAmount);

        supplementary.scheduleAtFixedRate(
                () -> {
                    long next = Math.max(capacity, token.get() + replenishAmount);
                    token.set(next); // TODO : set is not enough for concurrency
                },
                0,
                this.replenishDelay.toNanos(),
                TimeUnit.NANOSECONDS
        );
    }

    private static class DelayedTask {
        private final Runnable task;
        private final long whenToStart;

        public DelayedTask(Runnable task, long whenToStart) {
            this.task = task;
            this.whenToStart = whenToStart;
        }

        @Override
        public String toString() {
            return "DelayedTask{" +
                    "task=" + task +
                    ", whenToStart=" + whenToStart +
                    '}';
        }
    }


    @Override
    public boolean submit(Runnable task) {
        long now = ticker.now();
        if (token.getAndDecrement() > 0) {
            DelayedTask delayedTask = new DelayedTask(task, now);
            queue.offer(delayedTask);
        } else {
            // else calculate
            Duration durationToAdd = replenishDelay.multipliedBy(token.get() / replenishAmount);
            DelayedTask delayedTask = new DelayedTask(task, now + durationToAdd.toNanos());
            queue.offer(delayedTask);
        }
        start();
        return true;
    }

    private void start() {
        if (!running.get()) {
            synchronized (this) {
                if (!running.get()) {
                    logger.info("start to set");
                    running.set(true);
                    peeker.submit(this::peekAndRun);
                }
            }
        }
    }

    private void peekAndRun() {
        // 1. if queue is empty -> running = false;
        // 2. if peek is not yet.
        DelayedTask peek = queue.peek();
        long now = ticker.now();

        if (peek == null) {
            running.set(false);
            return;
        }

        if (peek.whenToStart <= now) {
            // time has come
            DelayedTask delayedTask = queue.poll();
            assert delayedTask != null;
            workers.submit(delayedTask.task);
            peek = queue.peek(); // peek second
        }

        if (peek == null) {
            running.set(false);
            return;
        }
        long delay = Math.min(0, peek.whenToStart - now);
        peeker.schedule(this::peekAndRun, delay, TimeUnit.NANOSECONDS);
    }

    @Override
    public void shutdown(Duration duration) {
        // TODO
    }
}
