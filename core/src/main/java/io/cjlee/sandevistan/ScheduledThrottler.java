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
    private final ScheduledExecutorService poller = Executors.newScheduledThreadPool(1);
    private volatile boolean started = false;


    public ScheduledThrottler(Duration interval) {
        this(interval, Executors.newFixedThreadPool(2));
    }

    public ScheduledThrottler(Duration interval, ExecutorService worker) {
        this.interval = interval;
        this.worker = worker;
    }

    private Runnable scheduleToTake() {
        return () -> {
            try {
                Runnable command = queue.poll(interval.toNanos(), TimeUnit.NANOSECONDS);
                if (command == null) {
                    return;
                }
                worker.submit(command);
                poller.schedule(scheduleToTake(), interval.toNanos(), TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                throw new IllegalStateException("running interrupted", e);
            }
        };
    }

    @Override
    public boolean submit(Runnable command) {
        if (!started) {
            started = true;
            poller.submit(scheduleToTake());
        }
        return queue.offer(command);
    }

    @Override
    public void shutdown() {
        poller.shutdown();
        worker.shutdown();
    }
}
