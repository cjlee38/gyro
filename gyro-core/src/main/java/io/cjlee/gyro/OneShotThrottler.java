package io.cjlee.gyro;

import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.scheduler.Scheduler;
import io.cjlee.gyro.task.Task;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

public class OneShotThrottler extends AbstractThrottler implements Throttler {
    public OneShotThrottler(Duration interval, ExecutorService worker, TaskQueue queue, Scheduler scheduler) {
        super(interval, worker, queue, scheduler);
    }

    @Override
    protected void executeSubmitted(long started, Duration timeout) {
        Task task = queue.poll(timeout);
        if (task == null) {
            return;
        }
        worker.execute(task);
    }
}
