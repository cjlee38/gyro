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

    public static void nanoSleep(Duration duration) {
        long sleepUntil = System.nanoTime() + duration.toNanos();
        while (System.nanoTime() < sleepUntil) {
            Thread.yield();
        }
    }
}
