package io.cjlee.gyro.support;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ThrottlerLatch {
    private static final Logger log = LoggerFactory.getLogger(ThrottlerLatch.class);

    protected final int expectCount;
    protected final VirtualTicker ticker;
    protected long started;

    private final CountDownLatch latch;
    private final List<Lap> laps = Collections.synchronizedList(new ArrayList<>());

    public ThrottlerLatch(int expectCount, VirtualTicker ticker) {
        this.expectCount = expectCount;
        this.ticker = ticker;

        this.latch = new CountDownLatch(expectCount);
        this.started = ticker == null ? System.nanoTime() : ticker.now();
    }

    public void lap() {
        long nano = now();
        Lap lap = new Lap(nano);
        this.laps.add(lap);

        latch.countDown();
        log.debug("lap - ({}, {})", expectCount - latch.getCount() - 1, lap);
    }

    public List<Lap> laps() {
        return new ArrayList<>(laps);
    }

    protected long now() {
        return (ticker == null ? System.nanoTime() : ticker.now()) - started;
    }

    protected void awaitLatch(Duration timeout) {
        try {
            boolean awaited = latch.await(timeout.toNanos(), TimeUnit.NANOSECONDS);
            if (!awaited) {
                throw new RuntimeException("too long to wait : " + timeout.toMillis() + "ms. Expected : " + expectCount
                        + " but count was : " + (expectCount - latch.getCount()));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
