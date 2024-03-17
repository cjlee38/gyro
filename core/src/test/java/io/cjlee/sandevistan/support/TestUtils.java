package io.cjlee.sandevistan.support;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private TestUtils() {
    }

    public static void repeat(int count, Runnable runnable) {
        repeat(count, Duration.ZERO, runnable);

    }

    public static void repeat(int count, Duration duration, Runnable runnable) {
        for (int i = 0; i < count; i++) {
            executor.schedule(runnable, duration.multipliedBy(i).toNanos(), TimeUnit.NANOSECONDS);
        }
    }
}
