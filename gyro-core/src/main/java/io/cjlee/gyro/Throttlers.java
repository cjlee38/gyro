package io.cjlee.gyro;

import io.cjlee.gyro.queue.MpscUnboundedTaskQueue;
import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.scheduler.ScheduledScheduler;
import io.cjlee.gyro.scheduler.Scheduler;
import io.cjlee.gyro.ticker.NativeTicker;
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
    private static final Supplier<Scheduler> DEFAULT_SCHEDULER_SUPPLER =
            () -> new ScheduledScheduler(new NativeTicker());

    public static Throttler oneShot(Duration interval) {
        return oneShot(interval,
                DEFAULT_WORKER_SUPPLIER.get(),
                DEFAULT_QUEUE_SUPPLIER.apply(2),
                DEFAULT_SCHEDULER_SUPPLER.get());
    }

    public static Throttler oneShot(Duration interval,
                                    ExecutorService executorService,
                                    TaskQueue queue,
                                    Scheduler scheduler) {
        return new OneShotThrottler(interval, executorService, queue, scheduler);
    }

    public static Throttler tokenBucket(int capacity,
                                        int replenishAmount,
                                        Duration replenishDelay) {
        return tokenBucket(capacity,
                replenishAmount,
                replenishDelay,
                DEFAULT_WORKER_SUPPLIER.get(),
                DEFAULT_QUEUE_SUPPLIER.apply(capacity),
                DEFAULT_SCHEDULER_SUPPLER.get());
    }

    public static Throttler tokenBucket(int capacity,
                                        int replenishAmount,
                                        Duration replenishDelay,
                                        ExecutorService worker,
                                        TaskQueue queue,
                                        Scheduler scheduler) {
        return new TokenBucketThrottler(capacity, replenishAmount, replenishDelay, worker, queue, scheduler);
    }

    public static Throttler fixedWindow(int windowSize, Duration interval) {
        return fixedWindow(windowSize,
                interval,
                DEFAULT_WORKER_SUPPLIER.get(),
                DEFAULT_QUEUE_SUPPLIER.apply(windowSize),
                DEFAULT_SCHEDULER_SUPPLER.get());
    }

    public static Throttler fixedWindow(int windowSize,
                                        Duration interval,
                                        ExecutorService worker,
                                        TaskQueue queue,
                                        Scheduler scheduler) {
        return new FixedWindowThrottler(windowSize, interval, worker, queue, scheduler);
    }

    public static ThrottlerCustomizer customize() {
        return new ThrottlerCustomizer();
    }
}
