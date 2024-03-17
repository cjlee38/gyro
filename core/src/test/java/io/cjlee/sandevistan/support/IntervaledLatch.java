package io.cjlee.sandevistan.support;

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
    private static final Logger logger = LoggerFactory.getLogger(IntervaledLatch.class);

    private final Duration interval;
    private final int expectCount;
    private final CountDownLatch countDownLatch;
    private final List<Instant> instants = Collections.synchronizedList(new ArrayList<>());

    public IntervaledLatch(Duration interval, int expectCount) {
        this.interval = interval;
        this.expectCount = expectCount;
        this.countDownLatch = new CountDownLatch(expectCount);
    }

    public void lap() {
        this.instants.add(Instant.now());
        countDownLatch.countDown();
        logger.debug("lap");
    }

    public List<Instant> instants() {
        awaitLatch();

        return new ArrayList<>(instants);
    }

    public boolean intervaled() {
        return this.intervaled(Duration.ofMillis(1), 0, expectCount - 1);
    }

    public boolean intervaled(Duration toleration) {
        return this.intervaled(toleration, 0, expectCount - 1);
    }

    public boolean intervaled(int start, int end) {
        return this.intervaled(Duration.ofMillis(1), start, end);
    }

    public boolean intervaled(Duration toleration, int start, int end) {
        awaitLatch();

        if (instants.size() <= 1) {
            return true;
        }

        boolean isSuccess = true;

        for (int i = start; i <= end - 1; i++) {
            Instant current = instants.get(i);
            Instant next = instants.get(i + 1);
            if (current.plus(interval.minus(toleration)).isAfter(next)) {
                logger.error(
                        "current = {}({}) <-[{}ms]-> next = {}({})",
                        i, current,
                        current.until(next, ChronoUnit.MILLIS),
                        i + 1, next
                );
                isSuccess = false;
            }
            logger.info(
                    "current = {}({}) <-[{}ms]-> next = {}({})",
                    i, current,
                    current.until(next, ChronoUnit.MILLIS),
                    i + 1, next
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

            boolean awaited = countDownLatch.await(
                    toWait.toNanos(),
                    TimeUnit.NANOSECONDS
            );
            if (!awaited) {
                throw new RuntimeException("too long to wait : " + toWait.toMillis() + "ms");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
