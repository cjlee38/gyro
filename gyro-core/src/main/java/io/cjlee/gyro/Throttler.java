package io.cjlee.gyro;

import java.time.Duration;
import java.util.List;

public interface Throttler {
    boolean submit(Runnable task); // TODO : return Future instead of boolean

    void shutdown(Duration duration);

    List<Runnable> shutdownNow();
}
