package io.cjlee.gyro;

public interface BoundedThrottler extends Throttler {
    void setOnDiscard(Runnable runnable);
}
