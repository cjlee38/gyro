package io.cjlee.sandevistan;

import java.time.Duration;

public interface Throttler {
    boolean submit(Runnable task); // TODO : return Future instead of boolean

    void shutdown(Duration duration);
}
