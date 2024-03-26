package io.cjlee.gyro.scheduler;

import io.cjlee.gyro.ticker.NativeTicker;
import io.cjlee.gyro.ticker.Ticker;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ScheduledScheduler implements Scheduler {
    private final ScheduledExecutorService service;
    private final Ticker ticker = new NativeTicker();

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
    public void shutdown(Consumer<Ticker> shutdown) {
        new Thread(() -> {
            shutdown.accept(ticker);
            service.shutdown();
        }).start();
    }

    @Override
    public void shutdownNow() {
        service.shutdownNow();
    }
}
