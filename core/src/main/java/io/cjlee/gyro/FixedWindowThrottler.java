package io.cjlee.gyro;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

public class FixedWindowThrottler extends AbstractThrottler implements Throttler {
    private final long windowSize;

    public FixedWindowThrottler(long windowSize, Duration interval, ExecutorService worker) {
        super(interval, worker);
        this.windowSize = windowSize;
    }

    @Override
    protected long concurrency() {
        return windowSize;
    }
}
