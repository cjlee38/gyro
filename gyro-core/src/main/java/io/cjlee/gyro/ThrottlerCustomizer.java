package io.cjlee.gyro;

import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.scheduler.Scheduler;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

public class ThrottlerCustomizer {

    private Duration interval;
    private int concurrency;
    
    private ExecutorService worker;
    private TaskQueue queue;
    private Scheduler scheduler;

    public ThrottlerCustomizer interval(Duration interval) {
        this.interval = interval;
        return this;
    }

    public ThrottlerCustomizer concurrency(int concurrency) {
        this.concurrency = concurrency;
        return this;
    }

    public ThrottlerCustomizer worker(ExecutorService worker) {
        this.worker = worker;
        return this;
    }

    public ThrottlerCustomizer queue(TaskQueue queue) {
        this.queue = queue;
        return this;
    }

    public ThrottlerCustomizer scheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    public Throttler build() {
        return new AbstractThrottler(interval, worker, queue, scheduler) {
            @Override
            protected int concurrency() {
                return concurrency;
            }
        };
    }
}
