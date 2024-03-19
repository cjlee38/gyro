package io.cjlee.gyro.utils;

import java.time.Duration;

public class ThreadUtils {
    private ThreadUtils() {
    }

    public static void trySleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
