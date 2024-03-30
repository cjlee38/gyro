package io.cjlee.gyro;

import io.cjlee.gyro.support.IntervaledLatch;
import io.cjlee.gyro.support.Lap;
import io.cjlee.gyro.support.TestUtils;
import io.cjlee.gyro.support.VirtualThrottlers;
import io.cjlee.gyro.support.VirtualTicker;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenBucketThrottlerTest {
    private VirtualTicker ticker;

    @BeforeEach
    void setUp() {
        ticker = new VirtualTicker();
    }

    @Test
    void immediate() {
        int capacity = 3;
        Throttler throttler = VirtualThrottlers.tokenBucket(capacity, 1, Duration.ofSeconds(1), ticker);
        IntervaledLatch latch = new IntervaledLatch(Duration.ZERO, capacity, ticker);
        Runnable runnable = latch::lap;

        TestUtils.repeat(capacity, () -> throttler.submit(runnable));
        latch.test()
                .assertAll(Lap.ofMillis(0), Lap.ofMillis(0), Lap.ofMillis(0))
                .end();
    }

    @Test
    void overflow() {
        int capacity = 3;
        int shot = 6;
        Duration interval = Duration.ofSeconds(1);
        IntervaledLatch latch = new IntervaledLatch(interval, shot, ticker);
        Throttler throttler = VirtualThrottlers.tokenBucket(capacity, 1, interval, ticker);
        Runnable runnable = latch::lap;

        TestUtils.repeat(shot, () -> throttler.submit(runnable));
        latch.test()
                .assertAll(Lap.ofMillis(0), Lap.ofMillis(0), Lap.ofMillis(0))
                .andAdvance(interval)
                .assertLast(Lap.ofMillis(1000))
                .andAdvance(interval)
                .assertLast(Lap.ofMillis(2000))
                .andAdvance(interval)
                .assertLast(Lap.ofMillis(3000))
                .endOn(2, 5);
    }

    @Test
    void breath() {
        int capacity = 3;
        int firstShot = 3;
        int secondShot = 3;
        Duration interval = Duration.ofSeconds(1);
        Throttler throttler = VirtualThrottlers.tokenBucket(capacity, 1, interval, ticker);
        IntervaledLatch latch = new IntervaledLatch(interval, firstShot + secondShot, ticker);
        Runnable runnable = latch::lap;

        TestUtils.repeat(firstShot, () -> throttler.submit(runnable));

        latch.test()
                .assertAll(Lap.ofMillis(0), Lap.ofMillis(0), Lap.ofMillis(0))
                .andAdvance(Duration.ofSeconds(1))
                .andDoWith(it -> {
                    it.advance(Duration.ofSeconds(1));
                    TestUtils.repeat(secondShot, () -> throttler.submit(runnable));
                })
                .assertRange(3, 5, Lap.ofMillis(2000), Lap.ofMillis(2000))
                .andAdvance(Duration.ofSeconds(1))
                .assertLast(Lap.ofMillis(3000))
                .endOn(4, 5);
    }

    @Test
    void shotPerSecond() {
        int capacity = 3;
        int shot = 6;
        Duration interval = Duration.ofSeconds(1);
        Throttler throttler = VirtualThrottlers.tokenBucket(capacity, 1, interval, ticker);
        IntervaledLatch latch = new IntervaledLatch(interval, shot, ticker);
        Runnable runnable = latch::lap;

        throttler.submit(runnable);
        latch.test()
                .andDo(() -> throttler.submit(runnable))
                .andAdvance(interval)
                .andDo(() -> throttler.submit(runnable))
                .andAdvance(interval)
                .andDo(() -> throttler.submit(runnable))
                .andAdvance(interval)
                .andDo(() -> throttler.submit(runnable))
                .andAdvance(interval)
                .andDo(() -> throttler.submit(runnable))
                .andAdvance(interval)
                .assertAll(Lap.ofMillis(0),
                        Lap.ofMillis(1000),
                        Lap.ofMillis(2000),
                        Lap.ofMillis(3000),
                        Lap.ofMillis(4000),
                        Lap.ofMillis(5000))
                .end();
    }
}
