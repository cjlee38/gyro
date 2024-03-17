package io.cjlee.sandevistan;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class Throttlers {
    private Throttlers() {
    }

    private static final Supplier<ExecutorService> DEFAULT_WORKER_SUPPLIER = Executors::newCachedThreadPool;

    public static Throttler oneShot(Duration interval) {
        return oneShot(interval, DEFAULT_WORKER_SUPPLIER.get());
    }

    public static Throttler oneShot(Duration interval, ExecutorService executorService) {
        return new OneShotThrottler(interval, executorService);
    }

    public static Throttler tokenBucket(long capacity,
                                        long replenishAmount,
                                        Duration replenishDelay) {
        return tokenBucket(capacity, replenishAmount, replenishDelay, DEFAULT_WORKER_SUPPLIER.get());
    }

    public static Throttler tokenBucket(long capacity,
                                        long replenishAmount,
                                        Duration replenishDelay,
                                        ExecutorService worker) {
        return new TokenBucketThrottler(capacity, replenishAmount, replenishDelay, worker);
    }
}
