package io.cjlee.gyro;

import io.cjlee.gyro.queue.TaskQueue;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

public class FixedWindowThrottler extends AbstractThrottler implements Throttler {
    private final long windowSize;

    public FixedWindowThrottler(long windowSize, Duration interval, ExecutorService worker, TaskQueue queue) {
        super(interval, worker, queue);
        this.windowSize = windowSize;
    }

    @Override
    protected long concurrency() {
        return windowSize;
    }
}
