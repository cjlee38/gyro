package io.cjlee.gyro.scheduler;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledScheduler implements Scheduler {
    private final ScheduledExecutorService service;

    public ScheduledScheduler() {
        this(Executors.newSingleThreadScheduledExecutor());
    }

    public ScheduledScheduler(ScheduledExecutorService service) {
        this.service = service;
    }

    @Override
    public void schedule(Runnable runnable) {
        this.schedule(runnable, Duration.ZERO);
    }

    @Override
    public void schedule(Runnable runnable, Duration interval) {
        service.schedule(runnable, interval.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    public void shutdown() {
        service.shutdown();
    }

    @Override
    public void shutdownNow() {
        service.shutdownNow();
    }
}
