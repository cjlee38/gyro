package io.cjlee.gyro.support;

import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntervaledLatch extends ThrottlerLatch {
    private static final Logger log = LoggerFactory.getLogger(IntervaledLatch.class);

    private final Duration interval;
    private final CountDownLatch countDownLatch;

    public IntervaledLatch(Duration interval, int expectCount, VirtualTicker ticker) {
        super(expectCount, ticker);
        this.interval = interval;
        this.countDownLatch = new CountDownLatch(expectCount);
    }

    public void advance(Duration interval) {
        Duration advance = interval == null ? this.interval : interval;
        if (ticker != null) {
            ticker.advance(advance);
        }
    }

    public boolean isIntervaled() {
        return this.isIntervaled(0, expectCount - 1);
    }

    public boolean isIntervaled(int start, int end) {
        awaitLatch(interval.multipliedBy(expectCount * 3));

        List<Lap> laps = laps();
        if (laps.size() <= 1) {
            return true;
        }

        boolean isSuccess = true;
        for (int i = start; i <= end - 1; i++) {
            Lap currentLap = laps.get(i);
            Instant currentInstant = currentLap.instant();
            Lap nextLap = laps.get(i + 1);
            Instant nextInstant = nextLap.instant();
            if (currentInstant.plus(interval).isAfter(nextInstant)) {
                log.error(
                        "current = {}({}) <-[{}ms]-> next = {}({})",
                        i, currentLap,
                        currentInstant.until(nextInstant, ChronoUnit.MILLIS),
                        i + 1, nextLap
                );
                isSuccess = false;
                continue;
            }
            log.info(
                    "current = {}({}) <-[{}ms]-> next = {}({})",
                    i, currentLap,
                    currentInstant.until(nextInstant, ChronoUnit.MILLIS),
                    i + 1, nextLap
            );
        }
        return isSuccess;
    }

    public IntervaledLatchAssertion test() {
        ThreadUtils.trySleep(Duration.ofMillis(50L));
        return new IntervaledLatchAssertion(this);
    }
}
