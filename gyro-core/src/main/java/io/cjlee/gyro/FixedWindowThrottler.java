package io.cjlee.gyro;

import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.scheduler.Scheduler;
import io.cjlee.gyro.task.Task;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

public class FixedWindowThrottler extends AbstractThrottler implements Throttler {
    private final int windowSize;

    public FixedWindowThrottler(int windowSize,
                                Duration interval,
                                ExecutorService worker,
                                TaskQueue queue,
                                Scheduler scheduler) {
        super(interval, worker, queue, scheduler);
        this.windowSize = windowSize;
    }

    @Override
    protected void executeSubmitted(long started, Duration timeout) {
        long concurrency = windowSize;

        while (concurrency-- > 0) {
            Task task = queue.poll(timeout);
            if (task == null) {
                break;
            }
            timeout = timeout.minusNanos(ticker.elapsed(started));
            worker.execute(task);
        }
    }
}
