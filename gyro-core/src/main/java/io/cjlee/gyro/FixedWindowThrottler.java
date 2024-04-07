package io.cjlee.gyro;

import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.scheduler.Scheduler;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

public class FixedWindowThrottler extends AbstractThrottler implements BoundedThrottler {
    private final int windowSize;
    private Runnable onDiscard;

    public FixedWindowThrottler(int windowSize,
                                Duration interval,
                                ExecutorService worker,
                                TaskQueue queue,
                                Scheduler scheduler) {
        super(interval, worker, queue, scheduler);
        this.windowSize = windowSize;
    }

    @Override
    protected int concurrency() {
        return windowSize;
    }

    @Override
    protected Runnable onDiscard() {
        return this.onDiscard;
    }

    @Override
    public void setOnDiscard(Runnable onDiscard) {
        this.onDiscard = onDiscard;
    }
}
