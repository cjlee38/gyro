package io.cjlee.gyro.nativetest;

import static org.assertj.core.api.Assertions.assertThat;

import io.cjlee.gyro.Throttler;
import io.cjlee.gyro.Throttlers;
import io.cjlee.gyro.support.IntervaledLatch;
import io.cjlee.gyro.support.TestUtils;
import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TokenBucketThrottlerTest {
    private static final Logger log = LoggerFactory.getLogger(TokenBucketThrottlerTest.class);

    @Test
    void immediate() {
        int capacity = 3;
        Throttler throttler = Throttlers.tokenBucket(capacity, 1, Duration.ofSeconds(1));
        IntervaledLatch latch = new IntervaledLatch(Duration.ZERO, capacity, null);
        Runnable runnable = latch::lap;

        TestUtils.repeat(capacity, () -> throttler.submit(runnable));

        assertThat(latch.intervaled()).isTrue();
    }

    @Test
    void overflow() {
        int capacity = 3;
        int shot = 6;
        Duration interval = Duration.ofSeconds(1);
        IntervaledLatch latch = new IntervaledLatch(interval, shot, null);
        Throttler throttler = Throttlers.tokenBucket(capacity, 1, interval);
        Runnable runnable = latch::lap;

        TestUtils.repeat(shot, () -> throttler.submit(runnable));

//        assertThat(latch.intervaled(2, 5)).isTrue();
        assertThat(latch.intervaled()).isTrue();
    }

    @Test
    void breath() {
        int capacity = 3;
        int firstShot = 3;
        int secondShot = 3;
        Duration interval = Duration.ofSeconds(1);
        Throttler throttler = Throttlers.tokenBucket(capacity, 1, interval);
        IntervaledLatch latch = new IntervaledLatch(interval, firstShot + secondShot, null);
        Runnable runnable = latch::lap;

        TestUtils.repeat(firstShot, () -> throttler.submit(runnable));
        ThreadUtils.trySleep(Duration.ofSeconds(2));
        TestUtils.repeat(secondShot, () -> throttler.submit(runnable));

        assertThat(latch.intervaled(2, 3)).isTrue();
        assertThat(latch.intervaled(4, 5)).isTrue();
    }

    @Test
    void shotPerSecond() {
        int shot = 6;
        Duration interval = Duration.ofSeconds(1);
        Throttler throttler = Throttlers.tokenBucket(3, 1, interval);
        IntervaledLatch latch = new IntervaledLatch(interval, shot, null);
        Runnable runnable = latch::lap;

        TestUtils.repeat(shot, interval, () -> throttler.submit(runnable));

        assertThat(latch.intervaled()).isTrue();
    }
}
