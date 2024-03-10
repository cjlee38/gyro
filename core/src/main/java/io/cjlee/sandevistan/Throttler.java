package io.cjlee.sandevistan;

import java.time.Duration;

public interface Throttler {
    boolean submit(Runnable command);

    void shutdown(Duration duration);
}
