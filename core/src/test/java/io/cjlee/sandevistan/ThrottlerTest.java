package io.cjlee.sandevistan;

import static org.assertj.core.api.Assertions.assertThat;

import io.cjlee.sandevistan.support.IntervaledLatch;
import io.cjlee.sandevistan.support.TestUtils;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ThrottlerTest {
    @Test
    void immediate() {
        int count = 3;
        Duration interval = Duration.ofMillis(1000L);
        IntervaledLatch intervaledLatch = new IntervaledLatch(interval, count);

        Throttler throttler = new ScheduledThrottler(interval);
        Runnable command = intervaledLatch::lap;
        TestUtils.repeat(count, () -> throttler.submit(command));

        assertThat(intervaledLatch.intervaled()).isTrue();
    }

    @Test
    void delayedSubmit() throws InterruptedException {
        int count = 2;
        Duration interval = Duration.ofMillis(1000);
        Throttler throttler = new ScheduledThrottler(interval);
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

        Throttler throttler = new ScheduledThrottler(interval);
        Runnable command = () -> {
            try {
                startIntervals.lap();
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
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

        Throttler throttler = new ScheduledThrottler(interval);
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

        Throttler throttler = new ScheduledThrottler(interval);
        TestUtils.repeat(count, () -> throttler.submit(() -> {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            intervaledLatch.lap();
        }));
        throttler.shutdown(interval.multipliedBy(count));
        assertThat(intervaledLatch.intervaled()).isTrue();
    }
}
