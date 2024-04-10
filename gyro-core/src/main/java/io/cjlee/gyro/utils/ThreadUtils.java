package io.cjlee.gyro.utils;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

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

    // Is it possible to enhance to REAL nano-sleep using hrtimers ?
    public static void nanoSleep(Duration duration) {
        long nanos = duration.toNanos();
        long sleepUntil = System.nanoTime() + nanos;
        while (System.nanoTime() < sleepUntil) {
            LockSupport.parkNanos(nanos);
        }
    }
}
