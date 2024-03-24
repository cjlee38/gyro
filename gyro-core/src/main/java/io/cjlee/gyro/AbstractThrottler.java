package io.cjlee.gyro;

import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.scheduler.ScheduledScheduler;
import io.cjlee.gyro.task.DefaultTask;
import io.cjlee.gyro.task.FutureTask;
import io.cjlee.gyro.task.Task;
import io.cjlee.gyro.ticker.NativeTicker;
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

    private final Duration interval;
    private final TaskQueue queue;
    private final ScheduledScheduler poller = new ScheduledScheduler();
    private final ExecutorService worker;

    private final Ticker ticker = new NativeTicker();

    private volatile boolean started = false;
    private volatile boolean shutdown = false;
    private volatile boolean terminated = false;

    public AbstractThrottler(Duration interval, ExecutorService worker, TaskQueue queue) {
        this.interval = interval;
        this.worker = worker;
        this.queue = queue;
    }

    @Override
    public Future<?> submit(Runnable task) {
        start();

        return submit0(wrap(task));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        start();

        return submit0(wrap(task));
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
            poller.schedule(this::onInterval);
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
        List<Task> tasksToExecute = new ArrayList<>((int) concurrency);
        while (concurrency-- > 0) {
            Task task = queue.peek();
            if (task == null) {
                break;
            }
            if (task.runnable()) {
                tasksToExecute.add(queue.poll());
            }
        }
        if (!tasksToExecute.isEmpty()) {
            tasksToExecute.get(0).onPrevious(() -> poller.schedule(this::onInterval, interval));
            tasksToExecute.forEach(worker::execute);
        } else {
            worker.execute(() -> poller.schedule(this::onInterval, interval));
        }
    }

    protected abstract long concurrency();

    private <T> Future<T> submit0(FutureTask<T> task) {
        if (shutdown) {
            return task;
        }
        if (!offerTask(task)) {
            task.reject();
            return task;
        }
        // Here we double-check whether the throttler shutdown since the task offered.
        if (shutdown) {
            queue.remove(task); // TODO : in case of already polled, so failed to remove ?
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

        long initiated = ticker.now();
        while (initiated + duration.toNanos() > ticker.now()) {
            if (!(queue.isEmpty() && terminated)) {
                ThreadUtils.trySleep(Duration.ofMillis(50L));
                continue;
            }
            log.info("Shutdown underlying poller and workers");
            poller.shutdown();
            worker.shutdown();
            return;
        }
        shutdownNow();
    }

    @Override
    public List<Runnable> shutdownNow() {
        poller.shutdownNow();
        List<Runnable> incomplete = new ArrayList<>(worker.shutdownNow());
        queue.drainTo(incomplete);

        return incomplete;
    }
}
