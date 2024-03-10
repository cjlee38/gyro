package io.cjlee.sandevistan.support;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TimeIntervals {
    private final Duration interval;
    private final List<Instant> instants = new ArrayList<>();

    public TimeIntervals(Duration interval) {
        this.interval = interval;
    }

    public void add(Instant instant) {
        this.instants.add(instant);
    }

    public boolean intervaled() {
        if (instants.size() <= 1) return true;

        for (int i = 0; i < instants.size() - 1; i++) {
            Instant current = instants.get(i);
            Instant next = instants.get(i + 1);
            if (current.plus(interval).isAfter(next)) {
                return false;
            }
        }
        return true;
    }

}
