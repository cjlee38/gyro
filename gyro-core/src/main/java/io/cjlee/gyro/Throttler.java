package io.cjlee.gyro;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface Throttler {
    Future<?> submit(Runnable task);

    <T> Future<T> submit(Callable<T> task);

    void shutdown(Duration duration);

    List<Runnable> shutdownNow();
}
