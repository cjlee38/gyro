package io.cjlee.gyro.support;

import static org.assertj.core.api.Assertions.assertThat;

import io.cjlee.gyro.utils.ListUtils;
import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntervaledLatchAssertion {
    private static final Logger log = LoggerFactory.getLogger(IntervaledLatchAssertion.class);
    private final IntervaledLatch intervaledLatch;

    public IntervaledLatchAssertion(IntervaledLatch intervaledLatch) {
        ThreadUtils.trySleep(Duration.ofMillis(50L));
        this.intervaledLatch = intervaledLatch;
    }

    public IntervaledLatchAssertion assertCount(int count) {
        assertThat(intervaledLatch.laps()).hasSize(count);
        return this;
    }

    public IntervaledLatchAssertion assertLast(Lap lap) {
        logOnFailure(() -> {
            Lap last = ListUtils.last(intervaledLatch.laps());
            assertThat(last).isEqualTo(lap);
        });
        return this;
    }

    public IntervaledLatchAssertion assertRange(int start, int end, Lap... laps) {
        logOnFailure(() -> assertThat(intervaledLatch.laps().subList(start, end)).containsExactly(laps));
        return this;
    }

    public IntervaledLatchAssertion assertAll(Lap... laps) {
        logOnFailure(() -> assertThat(intervaledLatch.laps()).containsExactly(laps));
        return this;
    }

    public IntervaledLatchAssertion andDo(Runnable runnable) {
        runnable.run();
        ThreadUtils.trySleep(Duration.ofMillis(100L));
        return this;
    }

    public IntervaledLatchAssertion andAdvance(Duration... intervals) {
        for (Duration interval : intervals) {
            intervaledLatch.advance(interval);
            ThreadUtils.trySleep(Duration.ofMillis(100L));
        }
        return this;
    }

    public IntervaledLatchAssertion andDoWith(Consumer<IntervaledLatch> consumer) {
        consumer.accept(intervaledLatch);
        ThreadUtils.trySleep(Duration.ofMillis(100L));
        return this;
    }

    public void end() {
        assertThat(intervaledLatch.intervaled()).isTrue();
    }

    public void endOn(int start, int end) {
        assertThat(intervaledLatch.intervaled(start, end)).isTrue();
    }

    private void logOnFailure(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            log.debug("Last intervaledLatch status : " + intervaledLatch.laps());
            throw e;
        }
    }
}
