package io.cjlee.sandevistan;

public interface Throttler {
    boolean submit(Runnable command);

    void shutdown();
}
