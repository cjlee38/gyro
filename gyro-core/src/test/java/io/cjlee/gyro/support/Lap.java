package io.cjlee.gyro.support;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Lap {
    private final long tick;

    public Lap(long tick) {
        this.tick = tick;
    }

    public static Lap ofMillis(long millis) {
        return new Lap(TimeUnit.MILLISECONDS.toNanos(millis));
    }

    public Instant instant() {
        Duration duration = Duration.ofNanos(tick);
        return Instant.ofEpochSecond(duration.getSeconds(), duration.getNano());
    }

    @Override
    public String toString() {
//        return instant().toString();
        Duration duration = Duration.ofNanos(tick);
        return "Lap(%dms)".formatted(duration.toMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Lap lap = (Lap) o;
        return tick == lap.tick;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tick);
    }

    public boolean isMatched(long tick, Duration toleration) {
        return Math.abs(tick - this.tick) < toleration.toNanos();
    }
}
