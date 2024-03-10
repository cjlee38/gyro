package io.cjlee.sandevistan;

import static org.assertj.core.api.Assertions.assertThat;

import io.cjlee.sandevistan.support.TestUtils;
import io.cjlee.sandevistan.support.TimeIntervals;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ThrottlerTest {
    private static final Logger logger = LoggerFactory.getLogger(ThrottlerTest.class);

    @Test
    void immediate() throws Exception {
        int count = 3;
        Duration interval = Duration.ofMillis(1000L);
        TimeIntervals timeIntervals = new TimeIntervals(interval);

        CountDownLatch latch = new CountDownLatch(count);
        Throttler throttler = new ScheduledThrottler(interval);
        Runnable command = () -> {
            timeIntervals.add(Instant.now());
            latch.countDown();
        };
        TestUtils.repeat(count, () -> throttler.submit(command));
        latch.await();

        assertThat(latch.getCount()).isZero();
        assertThat(timeIntervals.intervaled()).isTrue();
    }

    @Test
    void delayedSubmit() throws InterruptedException {
        int count = 2;
        Duration interval = Duration.ofMillis(1000);
        Throttler throttler = new ScheduledThrottler(interval);
        TimeIntervals timeIntervals = new TimeIntervals(interval);

        CountDownLatch latch = new CountDownLatch(count);

        Runnable command = () -> {
            timeIntervals.add(Instant.now());
            latch.countDown();
        };
        Thread.sleep(500L);
        TestUtils.repeat(count, () -> throttler.submit(command));
        latch.await();

        assertThat(timeIntervals.intervaled()).isTrue();
    }

    @Test
    void takesLong() throws InterruptedException {
        int count = 5;
        Duration interval = Duration.ofMillis(1000L);
        TimeIntervals startIntervals = new TimeIntervals(interval);
        TimeIntervals completeIntervals = new TimeIntervals(interval);

        CountDownLatch latch = new CountDownLatch(count);
        Throttler throttler = new ScheduledThrottler(interval);
        Runnable command = () -> {
            try {
                startIntervals.add(Instant.now());
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            completeIntervals.add(Instant.now());
            latch.countDown();
        };
        TestUtils.repeat(count, () -> throttler.submit(command));

        latch.await();

        assertThat(startIntervals.intervaled()).isTrue();
        assertThat(completeIntervals.intervaled()).isTrue();
    }
}
