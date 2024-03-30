package io.cjlee.gyro.support;

import io.cjlee.gyro.scheduler.Scheduler;
import io.cjlee.gyro.ticker.Ticker;
import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class VirtualScheduler implements Scheduler {

    private final VirtualTicker ticker;
    private final ExecutorService service = Executors.newCachedThreadPool();
    private final AtomicLong prev;

    public VirtualScheduler(VirtualTicker ticker) {
        this.ticker = ticker;
        this.prev = new AtomicLong();
    }

    @Override
    public void schedule(Runnable runnable) {
        prev.set(ticker.now());
        this.schedule(runnable, Duration.ZERO);
    }

    @Override
    public void schedule(Runnable runnable, Duration interval) {
        long nextTick = prev.get() + interval.toNanos();
        service.execute(() -> {
            // TODO : Execute by lock or any other locking mechanism, not sleep (see also `IntervaledLatchAssertion`)
            while (nextTick > ticker.now()) {
                ThreadUtils.trySleep(Duration.ofMillis(50L));
            }
            prev.addAndGet(interval.toNanos()); // or prev.set(nextTick); ?
            runnable.run();
        });
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
        // TODO
    }
}
