package io.cjlee.gyro.support;

import io.cjlee.gyro.OneShotThrottler;
import io.cjlee.gyro.Throttler;
import io.cjlee.gyro.TokenBucketThrottler;
import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.scheduler.Scheduler;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public class VirtualThrottlers {
    private VirtualThrottlers() {
    }

    private static final Supplier<ExecutorService> DEFAULT_WORKER_SUPPLIER = Executors::newCachedThreadPool;
    private static final Supplier<TaskQueue> DEFAULT_QUEUE_SUPPLIER = LoggingTaskQueue::new;
    private static final Function<VirtualTicker, Scheduler> DEFAULT_SCHEDULER_SUPPLER = VirtualScheduler::new;

    public static Throttler oneShot(Duration interval, VirtualTicker ticker) {
        return new OneShotThrottler(interval,
                DEFAULT_WORKER_SUPPLIER.get(),
                DEFAULT_QUEUE_SUPPLIER.get(),
                DEFAULT_SCHEDULER_SUPPLER.apply(ticker));
    }

    public static Throttler tokenBucket(Duration interval, VirtualTicker ticker) {
        return new OneShotThrottler(interval,
                DEFAULT_WORKER_SUPPLIER.get(),
                DEFAULT_QUEUE_SUPPLIER.get(),
                DEFAULT_SCHEDULER_SUPPLER.apply(ticker));
    }

    public static Throttler tokenBucket(int capacity,
                                        int replenishAmount,
                                        Duration replenishDelay,
                                        VirtualTicker ticker) {
        return new TokenBucketThrottler(capacity,
                replenishAmount,
                replenishDelay,
                DEFAULT_WORKER_SUPPLIER.get(),
                DEFAULT_QUEUE_SUPPLIER.get(),
                DEFAULT_SCHEDULER_SUPPLER.apply(ticker));
    }
}
