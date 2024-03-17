package io.cjlee.gyro;

import io.cjlee.gyro.task.DefaultTask;
import io.cjlee.gyro.task.Task;
import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO : Guarantee to run after delay (current approximated)
public abstract class AbstractThrottler implements Throttler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractThrottler.class);

    private final Duration interval;

    private final LinkedBlockingQueue<Task> queue = new LinkedBlockingQueue<>(); // TODO : replace this to faster one
    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService worker;

    private volatile boolean started = false;
    private volatile boolean shutdown = false;
    private volatile boolean terminated = false;

    public AbstractThrottler(Duration interval, ExecutorService worker) {
        this.interval = interval;
        this.worker = worker;
    }

    @Override
    public boolean submit(Runnable task) {
        start();

        return submit0(task);
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
            poller.scheduleWithFixedDelay(this::onInterval, 0, interval.toNanos(), TimeUnit.NANOSECONDS);
        }
    }

    protected void onInterval() {
        long concurrency = concurrency();
        while (concurrency-- > 0) {
            Task task = queue.peek();
            if (task == null) {
                return;
            }
            if (task.runnable()) {
                task = queue.poll();
                assert task != null;
                worker.execute(task);
            }
        }
    }

    protected abstract long concurrency();

    private boolean submit0(Runnable task) {
        if (!shutdown) {
            queue.offer(wrap(task));
            // Here we double-check whether the throttler shutdown since the task offered.
            if (shutdown) {
                queue.remove(task);
                logger.info("Submitted task rejected because of shutdown");
                return false;
            }
        }
        return true;
    }

    protected Task wrap(Runnable runnable) {
        return new DefaultTask(runnable);
    }

    // TODO : current not working as expected. (submitted task not to be ran because of `worker` shutdown)
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
            if (!queue.isEmpty() || !terminated) {
                ThreadUtils.trySleep(Duration.ofMillis(50L));
                continue;
            }
            logger.info("Shutdown underlying poller and workers");
            poller.shutdown();
            worker.shutdown();
            break;
        }
        worker.shutdownNow();
        poller.shutdownNow();
    }
}
