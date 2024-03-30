package io.cjlee.gyro;

import static org.assertj.core.api.Assertions.assertThat;

import io.cjlee.gyro.support.IntervaledLatch;
import io.cjlee.gyro.support.Lap;
import io.cjlee.gyro.support.TestUtils;
import io.cjlee.gyro.support.VirtualThrottlers;
import io.cjlee.gyro.support.VirtualTicker;
import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class OneShotThrottlerTest {
    private VirtualTicker ticker;

    @BeforeEach
    void setUp() {
        ticker = new VirtualTicker();
    }

    @Test
    void immediate() {
        int count = 3;
        Duration interval = Duration.ofMillis(1000L);
        IntervaledLatch intervaledLatch = new IntervaledLatch(interval, count, ticker);
        Throttler throttler = VirtualThrottlers.oneShot(interval, ticker);
        Runnable command = intervaledLatch::lap;

        TestUtils.repeat(count, () -> throttler.submit(command));

        intervaledLatch.test()
                .assertLast(Lap.ofMillis(0))
                .andAdvance(Duration.ofMillis(1000))
                .assertLast(Lap.ofMillis(1000L))
                .andAdvance(Duration.ofMillis(1000))
                .assertLast(Lap.ofMillis(2000L))
                .end();
    }

    @Test
    void delayedSubmit() {
        int count = 2;
        Duration interval = Duration.ofMillis(1000);
        Throttler throttler = VirtualThrottlers.oneShot(interval, ticker);
        IntervaledLatch intervaledLatch = new IntervaledLatch(interval, count, ticker);
        Runnable command = intervaledLatch::lap;

        ticker.advance(Duration.ofMillis(1500L));
        TestUtils.repeat(count, () -> throttler.submit(command));

        intervaledLatch.test()
                .assertLast(Lap.ofMillis(1500))
                .andAdvance(Duration.ofMillis(1000))
                .assertLast(Lap.ofMillis(2500))
                .end();
    }

    @Test
    void submitAndLatelySubmit() {
        Duration interval = Duration.ofMillis(1000L);
        IntervaledLatch intervaledLatch = new IntervaledLatch(Duration.ofMillis(2000), 2, ticker);
        Throttler throttler = VirtualThrottlers.oneShot(interval, ticker);
        Runnable command = intervaledLatch::lap;

        intervaledLatch.test()
                .andDo(() -> throttler.submit(command))
                .assertLast(Lap.ofMillis(0))
                .andAdvance(Duration.ofMillis(1500))
                .andDo(() -> throttler.submit(command))
                .andAdvance(Duration.ofMillis(500))
                .assertLast(Lap.ofMillis(2000))
                .end();
    }

    @Test
    void shutdown() {
        int count = 3;
        Duration interval = Duration.ofMillis(1000L);
        IntervaledLatch intervaledLatch = new IntervaledLatch(interval, count, ticker);
        Throttler throttler = VirtualThrottlers.oneShot(interval, ticker);

        TestUtils.repeat(count, () -> throttler.submit(intervaledLatch::lap));
        intervaledLatch.test()
                .andDo(() -> ThreadUtils.trySleep(Duration.ofMillis(100))) // to ensure all tasks to be submitted
                .andDo(() -> throttler.shutdown(interval.multipliedBy(count * 2)))
                .andAdvance(Duration.ofMillis(1000), Duration.ofMillis(1000))
                .assertCount(count)
                .end();
    }

    @Test
    @Disabled("TODO")
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
//        IntervaledLatch intervaledLatch = new IntervaledLatch(interval, count, ticker);
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
