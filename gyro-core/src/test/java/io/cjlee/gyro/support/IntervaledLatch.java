package io.cjlee.gyro.support;

import io.cjlee.gyro.utils.ThreadUtils;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntervaledLatch {
    private static final Logger log = LoggerFactory.getLogger(IntervaledLatch.class);

    private final VirtualTicker ticker;
    private final Duration interval;
    private final int expectCount;
    private final CountDownLatch countDownLatch;
    private final List<Lap> instants = Collections.synchronizedList(new ArrayList<>());

    public IntervaledLatch(Duration interval, int expectCount, VirtualTicker ticker) {
        this.interval = interval;
        this.expectCount = expectCount;
        this.countDownLatch = new CountDownLatch(expectCount);
        this.ticker = ticker;
    }

    public List<Lap> laps() {
        return new ArrayList<>(instants);
    }

    public void advance(Duration interval) {
        Duration advance = interval == null ? this.interval : interval;
        if (ticker != null) {
            ticker.advance(advance);
        }
    }

    public void lap() {
        long nano = ticker != null ? ticker.now() : System.nanoTime();
        Lap lap = new Lap(nano);
        this.instants.add(lap);

        countDownLatch.countDown();
        log.debug("lap - ({}, {})", expectCount - countDownLatch.getCount(), lap);
    }

    public boolean intervaled() {
        return this.intervaled(0, expectCount - 1);
    }

    public boolean intervaled(int start, int end) {
        awaitLatch();

        if (instants.size() <= 1) {
            return true;
        }

        boolean isSuccess = true;
        for (int i = start; i <= end - 1; i++) {
            Lap currentLap = instants.get(i);
            Instant currentInstant = currentLap.instant();
            Lap nextLap = instants.get(i + 1);
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

    private void awaitLatch() {
        try {
            Duration toWait;
            if (this.interval.isZero()) {
                toWait = Duration.ofMillis(10000);
            } else {
                toWait = this.interval.multipliedBy(expectCount * 2L);
            }

            boolean awaited = countDownLatch.await(toWait.toNanos(), TimeUnit.NANOSECONDS);
            if (!awaited) {
                throw new RuntimeException("too long to wait : " + toWait.toMillis() + "ms. Expected : " + expectCount + " but count was : " + (expectCount - countDownLatch.getCount()));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public IntervaledLatchAssertion test() {
        ThreadUtils.trySleep(Duration.ofMillis(50L));
        return new IntervaledLatchAssertion(this);
    }
}
