package io.cjlee.gyro.support;

import io.cjlee.gyro.ticker.Ticker;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class VirtualTicker implements Ticker {
    private AtomicLong tick = new AtomicLong();

    @Override
    public long now() {
        return tick.get();
    }

    public long advance(Duration duration) {
        return tick.addAndGet(duration.toNanos());
    }
}
