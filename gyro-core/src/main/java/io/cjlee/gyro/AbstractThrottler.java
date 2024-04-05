package io.cjlee.gyro;

import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.scheduler.Scheduler;
import io.cjlee.gyro.task.DefaultTask;
import io.cjlee.gyro.task.FutureTask;
import io.cjlee.gyro.task.Task;
import io.cjlee.gyro.utils.ListUtils;
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

    private final Duration interval;
    private final ExecutorService worker;

    /* for internal behaviors */
    private final TaskQueue queue;
    private final Scheduler scheduler;

    private volatile boolean started = false;
    private volatile boolean shutdown = false;
    private volatile boolean terminated = false;

    public AbstractThrottler(Duration interval, ExecutorService worker, TaskQueue queue, Scheduler scheduler) {
        this.interval = interval;
        this.worker = worker;
        this.queue = queue;
        this.scheduler = scheduler;
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

    protected void onInterval() {
        if (terminated && queue.isEmpty()) {
            return;
        }
        executeSubmitted();
        if (shutdown && queue.isEmpty()) {
            terminated = true;
        }
    }

    private void executeSubmitted() {
        long concurrency = concurrency();
        List<Task> tasks = new ArrayList<>((int) concurrency);
        while (concurrency-- > 0) {
            Task task = queue.peek();
            if (task == null) {
                break;
            }
            if (task.runnable()) {
                tasks.add(queue.poll());
            }
        }

        if (tasks.isEmpty()) {
            worker.execute(() -> scheduler.schedule(this::onInterval, interval));
            return;
        }
        ListUtils.first(tasks).onPrevious(() -> scheduler.schedule(this::onInterval, interval));
        tasks.forEach(worker::execute);
    }

    protected abstract int concurrency();

    private <T> Future<T> submit0(FutureTask<T> task) {
        if (shutdown) {
            return task;
        }
        if (!offerTask(task)) {
            task.cancel(false);
            return task;
        }
        // Here we double-check whether the throttler shutdown since the task offered.
        if (shutdown) {
            task.cancel(true);
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
        return new DefaultTask<>(callable);
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
