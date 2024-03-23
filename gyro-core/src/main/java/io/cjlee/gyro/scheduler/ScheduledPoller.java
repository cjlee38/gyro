package io.cjlee.gyro.scheduler;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledPoller {
    private final ScheduledExecutorService service;

    public ScheduledPoller() {
        this(Executors.newSingleThreadScheduledExecutor());
    }

    public ScheduledPoller(ScheduledExecutorService service) {
        this.service = service;
    }

    public void execute(Runnable runnable) {
        service.execute(runnable);
    }

    public void schedule(Runnable runnable, Duration interval) {
        service.schedule(runnable, interval.toNanos(), TimeUnit.NANOSECONDS);
    }

    public void shutdown() {
        service.shutdown();
    }

    public void shutdownNow() {
        service.shutdownNow();
    }
}
