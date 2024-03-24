package io.cjlee.gyro;

import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.task.DefaultTask;
import io.cjlee.gyro.task.FutureTask;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenBucketThrottler extends AbstractThrottler implements Throttler {

    private static final Logger log = LoggerFactory.getLogger(TokenBucketThrottler.class);

    private final long capacity;
    private final long replenishAmount;
    private final AtomicLong token; // AtomicLong can be a bottleneck in case of contention.

    public TokenBucketThrottler(long capacity,
                                long replenishAmount,
                                Duration replenishDelay,
                                ExecutorService workers,
                                TaskQueue queue) {
        super(replenishDelay, workers, queue);
        this.capacity = capacity;
        this.replenishAmount = replenishAmount;
        this.token = new AtomicLong(capacity);
    }

    @Override
    protected FutureTask<?> wrap(Runnable runnable) {
        TokenTask<Void> task = new TokenTask<>(runnable);
        task.onPrevious(token::getAndDecrement);
        return task;
    }

    @Override
    protected <T> FutureTask<T> wrap(Callable<T> callable) {
        TokenTask<T> task = new TokenTask<>(callable);
        task.onPrevious(token::getAndDecrement);
        return task;
    }

    @Override
    protected void onInterval() {
        replenishToken();
        super.onInterval();
    }

    private void replenishToken() {
        token.updateAndGet(it -> Math.min(capacity, it + replenishAmount));
    }

    @Override
    protected long concurrency() {
        return Math.min(this.capacity, token.get());
    }

    private class TokenTask<T> extends DefaultTask<T> {
        public TokenTask(Runnable runnable) {
            super(runnable);
        }

        public TokenTask(Callable<T> callable) {
            super(callable);
        }

        @Override
        public boolean runnable() {
            return token.get() > 0;
        }
    }
}
