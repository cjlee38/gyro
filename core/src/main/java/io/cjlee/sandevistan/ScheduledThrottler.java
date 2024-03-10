package io.cjlee.sandevistan;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledThrottler implements Throttler {
    private final Duration interval;
    private final ExecutorService worker;

    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean started = false;

    public ScheduledThrottler(Duration interval) {
        this(interval, Executors.newFixedThreadPool(2));
    }

    public ScheduledThrottler(Duration interval, ExecutorService worker) {
        this.interval = interval;
        this.worker = worker;
    }

    @Override
    public boolean submit(Runnable command) {
        if (!started) {
            synchronized (this) {
                if (!started) {
                    started = true;
                    poller.submit(scheduleToPoll());
                }
            }
        }
        return queue.offer(command);
    }

    @Override
    public void shutdown() {
        // todo : check if this is enough
        poller.shutdown();
        worker.shutdown();
    }

    private Runnable scheduleToPoll() {
        return () -> {
            try {
                Runnable command = queue.poll(interval.toNanos(), TimeUnit.NANOSECONDS);
                poller.schedule(scheduleToPoll(), interval.toNanos(), TimeUnit.NANOSECONDS);
                if (command == null) {
                    return;
                }
                worker.submit(command);
            } catch (InterruptedException e) {
                throw new IllegalStateException("running interrupted", e);
            }
        };
    }
}
