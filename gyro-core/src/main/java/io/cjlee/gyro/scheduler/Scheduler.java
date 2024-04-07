package io.cjlee.gyro.scheduler;

import io.cjlee.gyro.ticker.Ticker;
import java.time.Duration;
import java.util.function.Consumer;

public interface Scheduler {
    void schedule(Runnable runnable);

    void schedule(Runnable runnable, Duration interval);

    void shutdown(Consumer<Ticker> condition);

    void shutdownNow();

    Ticker ticker();
}
