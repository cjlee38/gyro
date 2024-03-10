package io.cjlee.sandevistan;

import io.cjlee.sandevistan.utils.ThreadUtils;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledThrottler implements Throttler {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledThrottler.class);

    private static final Supplier<ExecutorService> DEFAULT_WORKER_SUPPLIER = Executors::newCachedThreadPool;

    private final Duration interval;
    private final ExecutorService worker;

    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean started = false;
    private volatile boolean shutdown = false;

    public ScheduledThrottler(Duration interval) {
        this(interval, DEFAULT_WORKER_SUPPLIER.get());
    }

    public ScheduledThrottler(Duration interval, ExecutorService worker) {
        this.interval = interval;
        this.worker = worker;
    }

    @Override
    public boolean submit(Runnable task) {
        if (!started) {
            synchronized (this) {
                if (!started) {
                    started = true;
                    poller.submit(scheduleToPoll());
                }
            }
        }

        if (!shutdown) {
            queue.offer(task);
            // Here we double-check whether the throttler shutdown since the task offered.
            if (shutdown) {
                queue.remove(task);
                logger.info("Submitted task rejected because of shutdown");
                return false;
            }
        }
        return true;
    }

    @Override
    public void shutdown(Duration duration) {
        if (duration.isNegative()) {
            throw new IllegalArgumentException("Duration must be zero or positive to shutdown.");
        }
        if (duration.isZero()) {
            poller.shutdownNow();
            worker.shutdownNow();
        }
        shutdown = true;

        long initiated = System.nanoTime();
        while (initiated < System.nanoTime() + duration.toNanos()) {
            if (!queue.isEmpty()) {
                ThreadUtils.trySleep(Duration.ofMillis(50L));
                continue;
            }
            logger.info("Shutdown underlying poller and workers");
            poller.shutdown();
            worker.shutdown();
            break;
        }
    }

    private Runnable scheduleToPoll() {
        return () -> {
            try {
                if (shutdown && queue.isEmpty()) {
                    logger.info("stop to polling because of shutdown");
                    return;
                }
                Runnable task = queue.poll(interval.toNanos(), TimeUnit.NANOSECONDS);
                poller.schedule(scheduleToPoll(), interval.toNanos(), TimeUnit.NANOSECONDS);
                if (task == null) {
                    return;
                }
                worker.submit(task);
            } catch (InterruptedException e) {
                throw new IllegalStateException("running interrupted", e);
            }
        };
    }
}
