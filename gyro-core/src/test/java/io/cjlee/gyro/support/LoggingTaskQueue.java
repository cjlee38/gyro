package io.cjlee.gyro.support;

import io.cjlee.gyro.queue.TaskQueue;
import io.cjlee.gyro.task.Task;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingTaskQueue implements TaskQueue {
    private static final Logger log = LoggerFactory.getLogger(LoggingTaskQueue.class);

    private final BlockingQueue<Task> queue = new LinkedBlockingQueue<>();

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public Task peek() {
        Task task = queue.peek();
        log.debug("LoggingTaskQueue.peek : " + task);
        return task;
    }

    @Override
    public Task poll() {
        Task task = queue.poll();
        log.debug("LoggingTaskQueue.poll : " + task);
        return task;
    }

    @Override
    public boolean offer(Task task) {
        log.debug("LoggingTaskQueue.offer : " + task);
        return queue.offer(task);
    }

    @Override
    public boolean remove(Task task) {
        log.debug("LoggingTaskQueue.remove : " + task);
        return queue.remove(task);
    }

    @Override
    public void drainTo(List<Runnable> dest) {
        queue.drainTo(dest);
        log.debug("LoggingTaskQueue.drainTo : " + dest);
    }
}
