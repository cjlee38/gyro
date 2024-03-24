package io.cjlee.gyro;

import io.cjlee.gyro.queue.TaskQueue;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

public class OneShotThrottler extends AbstractThrottler implements Throttler {
    public OneShotThrottler(Duration interval, ExecutorService worker, TaskQueue queue) {
        super(interval, worker, queue);
    }

    @Override
    protected long concurrency() {
        return 1;
    }
}
