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

    public static void repeat(int count, Duration interval, Runnable runnable) {
        EXECUTOR.schedule(new Runnable() {
            private int remainingRuns = count;

            @Override
            public void run() {
                if (remainingRuns-- > 0) {
                    long startTime = System.nanoTime();
                    try {
                        runnable.run();
                    } finally {
                        long executionTime = System.nanoTime() - startTime;
                        long delayForNextRun = interval.toNanos() - executionTime;
                        if (delayForNextRun < 0) {
                            // I assume that the given runnable is `throttler.submit`,
                            // but nevertheless if the execution took longer than the interval, run immediately.
                            delayForNextRun = 0;
                        }
                        EXECUTOR.schedule(this, delayForNextRun, TimeUnit.NANOSECONDS);
                    }
                }
            }
        }, interval.toNanos(), TimeUnit.NANOSECONDS);
    }

    public static void assertSoftly(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            log.error("Soft assertion failed : ", e);
        }
    }
}
