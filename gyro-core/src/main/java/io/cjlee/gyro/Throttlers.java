package io.cjlee.gyro;

import io.cjlee.gyro.queue.MpscUnboundedTaskQueue;
import io.cjlee.gyro.queue.TaskQueue;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public class Throttlers {
    private Throttlers() {
    }

    private static final Supplier<ExecutorService> DEFAULT_WORKER_SUPPLIER = Executors::newCachedThreadPool;
    private static final Function<Integer, TaskQueue> DEFAULT_QUEUE_SUPPLIER = MpscUnboundedTaskQueue::new;

    public static Throttler oneShot(Duration interval) {
        return oneShot(interval, DEFAULT_WORKER_SUPPLIER.get(), DEFAULT_QUEUE_SUPPLIER.apply(2));
    }

    public static Throttler oneShot(Duration interval, ExecutorService executorService, TaskQueue queue) {
        return new OneShotThrottler(interval, executorService, queue);
    }

    public static Throttler tokenBucket(int capacity,
                                        int replenishAmount,
                                        Duration replenishDelay) {
        return tokenBucket(capacity,
                replenishAmount,
                replenishDelay,
                DEFAULT_WORKER_SUPPLIER.get(),
                DEFAULT_QUEUE_SUPPLIER.apply(capacity));
    }

    public static Throttler tokenBucket(int capacity,
                                        int replenishAmount,
                                        Duration replenishDelay,
                                        ExecutorService worker,
                                        TaskQueue queue) {
        return new TokenBucketThrottler(capacity, replenishAmount, replenishDelay, worker, queue);
    }
}
