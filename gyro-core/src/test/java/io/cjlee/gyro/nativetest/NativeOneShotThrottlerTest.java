package io.cjlee.gyro.nativetest;

import static org.assertj.core.api.Assertions.assertThat;

import io.cjlee.gyro.Throttler;
import io.cjlee.gyro.Throttlers;
import io.cjlee.gyro.support.IntervaledLatch;
import io.cjlee.gyro.support.TestUtils;
import io.cjlee.gyro.support.TimestampLatch;
import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class NativeOneShotThrottlerTest {
    @Test
    void immediate() {
        int count = 3;
        Duration interval = Duration.ofMillis(1000L);
        IntervaledLatch intervaledLatch = new IntervaledLatch(interval, count, null);
        Throttler throttler = Throttlers.oneShot(interval);
        Runnable command = intervaledLatch::lap;

        TestUtils.repeat(count, () -> throttler.submit(command));

        assertThat(intervaledLatch.isIntervaled()).isTrue();
    }

    @Test
    void delayedSubmit() throws InterruptedException {
        int count = 2;
        Duration interval = Duration.ofMillis(1000);
        Throttler throttler = Throttlers.oneShot(interval);
        IntervaledLatch intervaledLatch = new IntervaledLatch(interval, count, null);
        Runnable command = intervaledLatch::lap;
        Thread.sleep(500L);

        TestUtils.repeat(count, () -> throttler.submit(command));

        assertThat(intervaledLatch.isIntervaled()).isTrue();
    }

    @Test
    void takesLong() {
        int count = 5;
        Duration interval = Duration.ofMillis(1000L);
        IntervaledLatch startIntervals = new IntervaledLatch(interval, count, null);
        IntervaledLatch completeIntervals = new IntervaledLatch(interval, count, null);
        Throttler throttler = Throttlers.oneShot(interval);
        Runnable command = () -> {
            startIntervals.lap();
            ThreadUtils.trySleep(Duration.ofMillis(2000L));
            completeIntervals.lap();
        };

        TestUtils.repeat(count, () -> throttler.submit(command));

        assertThat(startIntervals.isIntervaled()).isTrue();
        // completeIntervals could not be properly intervaled because of context-switch
        TestUtils.assertSoftly(() -> assertThat(completeIntervals.isIntervaled()).isTrue());
    }

    @Test
    void submitAndLatelySubmit() throws InterruptedException {
        Duration interval = Duration.ofMillis(1000L);
        IntervaledLatch intervaledLatch = new IntervaledLatch(interval, 2, null);
        Throttler throttler = Throttlers.oneShot(interval);
        Runnable command = intervaledLatch::lap;

        throttler.submit(command);
        Thread.sleep(1500);
        throttler.submit(command);

        assertThat(intervaledLatch.isIntervaled()).isTrue();
    }

    @Test
    void shutdown() {
        int count = 3;
        Duration interval = Duration.ofMillis(1000L);
        TimestampLatch intervaledLatch = new TimestampLatch(count, Duration.ofMillis(50), null);

        Throttler throttler = Throttlers.oneShot(interval);
        TestUtils.repeat(count, () -> throttler.submit(() -> {
            ThreadUtils.trySleep(Duration.ofMillis(500L));
            intervaledLatch.lap();
        }));

        ThreadUtils.trySleep(Duration.ofMillis(100)); // to ensure all tasks to be submitted
        throttler.shutdown(interval.multipliedBy(count)); // 3000ms

        assertThat(intervaledLatch.isMatched(500, 1500, 2500)).isTrue();
    }

    @Test
    void shutdownSubmitButReject() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong check = new AtomicLong(0);
        Throttler throttler = Throttlers.oneShot(Duration.ofMillis(1000L));

        throttler.submit(() -> {
            ThreadUtils.trySleep(Duration.ofMillis(500L));
            check.compareAndSet(0, 1);
            latch.countDown();
        });
        throttler.shutdown(Duration.ofMillis(1000L));
        Future<Boolean> future = throttler.submit(() -> check.compareAndSet(1, 2));

        latch.await();
        assertThat(future.isDone()).isFalse();
        assertThat(check.get()).isOne();
    }

    @Test
    void shutdownNow() {
//        int count = 3;
//        Duration interval = Duration.ofMillis(1000L);
//        IntervaledLatch intervaledLatch = new IntervaledLatch(interval, count);
//
//        Throttler throttler = Throttlers.oneShot(interval);
//        TestUtils.repeat(count, () -> throttler.submit(() -> {
//            ThreadUtils.trySleep(Duration.ofMillis(500L));
//            intervaledLatch.lap();
//        }));
//
//        ThreadUtils.trySleep(Duration.ofMillis(100));
//        throttler.shutdown(interval.multipliedBy(count));
//
//        assertThat(intervaledLatch.intervaled()).isTrue();
    }
}
