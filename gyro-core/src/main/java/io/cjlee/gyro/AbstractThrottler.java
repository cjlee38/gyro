package io.cjlee.gyro;

import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.scheduler.Scheduler;
import io.cjlee.gyro.task.DefaultTask;
import io.cjlee.gyro.task.FutureTask;
import io.cjlee.gyro.ticker.Ticker;
import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractThrottler implements Throttler {
    private static final Logger log = LoggerFactory.getLogger(AbstractThrottler.class);

    protected final Duration interval;
    protected final ExecutorService worker;

    /* for internal behaviors */
    protected final TaskQueue queue;
    private final Scheduler scheduler;
    protected final Ticker ticker;

    private volatile boolean started = false;
    private volatile boolean shutdown = false;
    private volatile boolean terminated = false;

    public AbstractThrottler(Duration interval, ExecutorService worker, TaskQueue queue, Scheduler scheduler) {
        this.interval = interval;
        this.worker = worker;
        this.queue = queue;
        this.scheduler = scheduler;
        this.ticker = scheduler.ticker();
    }

    @Override
    public Future<?> submit(Runnable task) {
        Future<?> f = submit0(wrap(task));
        start();
        return f;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Future<T> f = submit0(wrap(task));
        start();
        return f;
    }

    private void start() {
        if (started) {
            return;
        }
        synchronized (this) {
            if (started) {
                return;
            }
            started = true;
            scheduler.schedule(this::onInterval);
        }
    }

    private void onInterval() {
        if (terminated && queue.isEmpty()) {
            return;
        }
        long started = ticker.now();
        executeSubmitted(started, interval);
        Duration nextSchedule = interval.minus(Duration.ofNanos(ticker.elapsed(started)));
        if (nextSchedule.isNegative()) {
            nextSchedule = Duration.ZERO;
        }
        scheduler.schedule(this::onInterval, nextSchedule);

        if (shutdown && queue.isEmpty()) {
            terminated = true;
        }
    }

    protected abstract void executeSubmitted(long started, Duration timeout);

    private <T> Future<T> submit0(FutureTask<T> task) {
        if (shutdown) {
            return task;
        }
        if (!offerTask(task)) {
            task.discard(false);
            return task;
        }
        // Here we double-check whether the throttler shutdown since the task offered.
        if (shutdown) {
            task.discard(true);
            queue.remove(task);
            log.info("Submitted task rejected because of shutdown");
        }
        return task;
    }

    private <T> boolean offerTask(FutureTask<T> task) {
        return queue.offer(task);
    }

    protected FutureTask<?> wrap(Runnable runnable) {
        return new DefaultTask<>(runnable);
    }

    protected <T> FutureTask<T> wrap(Callable<T> callable) {
        DefaultTask<T> task = new DefaultTask<>(callable);
        task.onDiscarded(onDiscard());
        return task;
    }

    protected Runnable onDiscard() {
        return null;
    }

    @Override
    public void shutdown(Duration duration) {
        if (duration.isNegative()) {
            throw new IllegalArgumentException("Duration must be zero or positive to shutdown.");
        }
        shutdown = true;
        if (duration.isZero()) {
            shutdownNow();
            return;
        }

        scheduler.shutdown(ticker -> {
            long initiated = ticker.now();
            while (initiated + duration.toNanos() > ticker.now()) {
                if (!(queue.isEmpty() && terminated)) {
                    ThreadUtils.trySleep(Duration.ofMillis(50L));
                    continue;
                }
                log.info("Shutdown underlying scheduler and workers");
                worker.shutdown();
                return;
            }
        });
    }

    @Override
    public List<Runnable> shutdownNow() {
        terminated = true;

        scheduler.shutdownNow();
        List<Runnable> incomplete = new ArrayList<>(worker.shutdownNow());
        queue.drainTo(incomplete);

        return incomplete;
    }
}
