package io.cjlee.gyro.nativetest;

import static org.assertj.core.api.Assertions.assertThat;

import io.cjlee.gyro.Throttler;
import io.cjlee.gyro.Throttlers;
import io.cjlee.gyro.support.TestUtils;
import io.cjlee.gyro.support.TimestampLatch;
import io.cjlee.gyro.support.WarmUpTestBase;
import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class NativeTokenBucketThrottlerTest extends WarmUpTestBase {
    private static final Logger log = LoggerFactory.getLogger(NativeTokenBucketThrottlerTest.class);

    @Test
    void immediate() {

        int capacity = 3;
        Throttler throttler = Throttlers.tokenBucket(capacity, 1, Duration.ofSeconds(1));
        TimestampLatch latch = new TimestampLatch(3, Duration.ofMillis(50), null);
        Runnable runnable = latch::lap;

        TestUtils.repeat(capacity, () -> throttler.submit(runnable));

        assertThat(latch.isMatched(0, 333, 666)).isTrue();
    }

    @Test
    void overflow() {
        int capacity = 3;
        int shot = 6;
        Duration interval = Duration.ofSeconds(1);
        TimestampLatch latch = new TimestampLatch(6, Duration.ofMillis(50), null);
        Throttler throttler = Throttlers.tokenBucket(capacity, 1, interval);
        Runnable runnable = latch::lap;

        TestUtils.repeat(shot, () -> throttler.submit(runnable));

        assertThat(latch.isMatched(0, 333, 666, 1000, 2000, 3000)).isTrue();
    }

    @Test
    void breath() {
        int capacity = 3;
        int firstShot = 3;
        int secondShot = 3;
        Duration interval = Duration.ofSeconds(1);
        Throttler throttler = Throttlers.tokenBucket(capacity, 1, interval);
        TimestampLatch latch = new TimestampLatch(firstShot + secondShot, Duration.ofMillis(50), null);
        Runnable runnable = latch::lap;

        TestUtils.repeat(firstShot, () -> throttler.submit(runnable));
        ThreadUtils.trySleep(Duration.ofMillis(2100)); // wait for 100ms more, to ensure that interval has been passed twice.
        TestUtils.repeat(secondShot, () -> throttler.submit(runnable));

        assertThat(latch.isMatched(0, 333, 666, 2100, 2500, 3000)).isTrue();
    }

    @Test
    void shotPerSecond() {
        int shot = 6;
        Duration interval = Duration.ofSeconds(1);
        Throttler throttler = Throttlers.tokenBucket(3, 1, interval);
        TimestampLatch latch = new TimestampLatch(shot, Duration.ofMillis(50), null);
        Runnable runnable = latch::lap;

        TestUtils.repeat(shot, interval, () -> throttler.submit(runnable));

        assertThat(latch.isMatched(0, 1000, 2000, 3000, 4000, 5000)).isTrue();
    }
}
