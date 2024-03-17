package io.cjlee.gyro;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OneShotThrottler extends AbstractThrottler implements Throttler {
    public OneShotThrottler(Duration interval) {
        this(interval, Executors.newCachedThreadPool());
    }

    public OneShotThrottler(Duration interval, ExecutorService worker) {
        super(interval, worker);
    }

    @Override
    protected long concurrency() {
        return 1;
    }
}
