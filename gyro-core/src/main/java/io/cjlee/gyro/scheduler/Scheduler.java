package io.cjlee.gyro.scheduler;

import java.time.Duration;

public interface Scheduler {
    void schedule(Runnable runnable);

    void schedule(Runnable runnable, Duration interval);

    void shutdown();

    void shutdownNow();
}
