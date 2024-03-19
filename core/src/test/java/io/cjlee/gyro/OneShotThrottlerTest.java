package io.cjlee.gyro;

import static org.assertj.core.api.Assertions.assertThat;

import io.cjlee.gyro.support.IntervaledLatch;
import io.cjlee.gyro.support.TestUtils;
import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class OneShotThrottlerTest {
    @Test
    void immediate() {
        int count = 3;
        Duration interval = Duration.ofMillis(1000L);
        IntervaledLatch intervaledLatch = new IntervaledLatch(interval, count);
        Throttler throttler = Throttlers.oneShot(interval);
        Runnable command = intervaledLatch::lap;

        TestUtils.repeat(count, () -> throttler.submit(command));

        assertThat(intervaledLatch.intervaled()).isTrue();
    }

    @Test
    void delayedSubmit() throws InterruptedException {
        int count = 2;
        Duration interval = Duration.ofMillis(1000);
        Throttler throttler = Throttlers.oneShot(interval);
        IntervaledLatch intervaledLatch = new IntervaledLatch(interval, count);
        Runnable command = intervaledLatch::lap;
        Thread.sleep(500L);

        TestUtils.repeat(count, () -> throttler.submit(command));

        assertThat(intervaledLatch.intervaled()).isTrue();
    }

    @Test
    void takesLong() {
        int count = 5;
        Duration interval = Duration.ofMillis(1000L);
        IntervaledLatch startIntervals = new IntervaledLatch(interval, count);
        IntervaledLatch completeIntervals = new IntervaledLatch(interval, count);
        Throttler throttler = Throttlers.oneShot(interval);
        Runnable command = () -> {
            startIntervals.lap();
            ThreadUtils.trySleep(Duration.ofMillis(2000L));
            completeIntervals.lap();
        };

        TestUtils.repeat(count, () -> throttler.submit(command));

        assertThat(startIntervals.intervaled()).isTrue();
        assertThat(completeIntervals.intervaled()).isTrue();
    }

    @Test
    void submitAndLatelySubmit() throws InterruptedException {
        Duration interval = Duration.ofMillis(1000L);
        IntervaledLatch intervaledLatch = new IntervaledLatch(interval, 2);
        Throttler throttler = Throttlers.oneShot(interval);
        Runnable command = intervaledLatch::lap;

        throttler.submit(command);
        Thread.sleep(1500);
        throttler.submit(command);

        assertThat(intervaledLatch.intervaled()).isTrue();
    }

    @Test
    void shutdown() {
        int count = 3;
        Duration interval = Duration.ofMillis(1000L);
        IntervaledLatch intervaledLatch = new IntervaledLatch(interval, count);

        Throttler throttler = Throttlers.oneShot(interval);
        TestUtils.repeat(count, () -> throttler.submit(() -> {
            ThreadUtils.trySleep(Duration.ofMillis(500L));
            intervaledLatch.lap();
        }));

        ThreadUtils.trySleep(Duration.ofMillis(100)); // to ensure all tasks to be submitted
        throttler.shutdown(interval.multipliedBy(count));

        assertThat(intervaledLatch.intervaled()).isTrue();
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
        boolean submitted = throttler.submit(() -> check.compareAndSet(1, 2));

        latch.await();
        assertThat(submitted).isFalse();
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
