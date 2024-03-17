package io.cjlee.gyro;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

public class OneShotThrottler extends AbstractThrottler implements Throttler {
    public OneShotThrottler(Duration interval, ExecutorService worker) {
        super(interval, worker);
    }

    @Override
    protected long concurrency() {
        return 1;
    }
}
