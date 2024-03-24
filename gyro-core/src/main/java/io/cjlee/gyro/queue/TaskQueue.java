package io.cjlee.gyro.queue;

import io.cjlee.gyro.task.Task;
import java.util.List;

public interface TaskQueue {
    boolean isEmpty();

    Task peek();

    Task poll();

    boolean offer(Task task);

    boolean remove(Task task);

    void drainTo(List<Runnable> to);
}
