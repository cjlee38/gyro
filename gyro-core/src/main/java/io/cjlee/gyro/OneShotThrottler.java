package io.cjlee.gyro;

import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.marker.Unbounded;
import io.cjlee.gyro.scheduler.Scheduler;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

@Unbounded
public class OneShotThrottler extends AbstractThrottler implements Throttler {
    public OneShotThrottler(Duration interval, ExecutorService worker, TaskQueue queue, Scheduler scheduler) {
        super(interval, worker, queue, scheduler);
    }

    @Override
    protected int concurrency() {
        return 1;
    }
}
