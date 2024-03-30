package io.cjlee.gyro.support;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {
    private static final Logger log = LoggerFactory.getLogger(TestUtils.class);
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private TestUtils() {
    }

    public static void repeat(int count, Runnable runnable) {
        repeat(count, Duration.ZERO, runnable);
    }

    public static void repeat(int count, Duration duration, Runnable runnable) {
        for (int i = 0; i < count; i++) {
            EXECUTOR.schedule(runnable, duration.multipliedBy(i).toNanos(), TimeUnit.NANOSECONDS);
        }
    }
}
