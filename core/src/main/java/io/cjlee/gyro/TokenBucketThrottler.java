package io.cjlee.gyro;

import io.cjlee.gyro.task.DefaultTask;
import io.cjlee.gyro.task.Task;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenBucketThrottler extends AbstractThrottler implements Throttler {

    private static final Logger logger = LoggerFactory.getLogger(TokenBucketThrottler.class);

    private final long capacity;
    private final long replenishAmount;
    private final AtomicLong token; // AtomicLong could be bottl-neck in case of contention.

    public TokenBucketThrottler(long capacity, long replenishAmount, Duration replenishDelay, ExecutorService workers) {
        super(replenishDelay, workers);
        this.capacity = capacity;
        this.replenishAmount = replenishAmount;
        this.token = new AtomicLong(capacity);
    }

    @Override
    protected Task wrap(Runnable runnable) {
        return new TokenTask(runnable);
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

    private class TokenTask extends DefaultTask {
        public TokenTask(Runnable runnable) {
            super(runnable);
        }

        @Override
        public boolean runnable() {
            return token.get() > 0;
        }

        @Override
        public void run() {
            token.decrementAndGet();
            super.run();
        }
    }
}
