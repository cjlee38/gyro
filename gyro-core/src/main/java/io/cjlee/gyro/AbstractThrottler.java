package io.cjlee.gyro;

import io.cjlee.gyro.task.DefaultTask;
import io.cjlee.gyro.task.Task;
import io.cjlee.gyro.ticker.NativeTicker;
import io.cjlee.gyro.ticker.Ticker;
import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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

    private final Ticker ticker = new NativeTicker();

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
        if (terminated && queue.isEmpty()) {
            return;
        }
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
        if (shutdown && queue.isEmpty()) {
            terminated = true;
        }
    }

    protected abstract long concurrency();

    private boolean submit0(Runnable task) {
        if (shutdown) {
            return false;
        }
        queue.offer(wrap(task));
        // Here we double-check whether the throttler shutdown since the task offered.
        if (shutdown) {
            queue.remove(task);
            logger.info("Submitted task rejected because of shutdown");
            return false;
        }
        return true;
    }

    protected Task wrap(Runnable runnable) {
        return new DefaultTask(runnable);
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
            logger.info("Shutdown underlying poller and workers");
            poller.shutdown();
            worker.shutdown();
            return;
        }
        shutdownNow();
    }

    @Override
    public List<Runnable> shutdownNow() {
        List<Runnable> incomplete = new ArrayList<>();
        queue.drainTo(incomplete);
        poller.shutdownNow();

        incomplete.addAll(worker.shutdownNow());
        return incomplete;
    }
}
