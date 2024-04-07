package io.cjlee.gyro.queue;

import io.cjlee.gyro.task.Task;
import java.time.Duration;
import java.util.List;
import java.util.Queue;

public abstract class DelegateQueue implements TaskQueue {
    protected final Queue<Task> queue;

    public DelegateQueue(Queue<Task> queue) {
        this.queue = queue;
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public Task peek() {
        return queue.peek();
    }

    @Override
    public Task poll(Duration timeout) {
        // TODO : lead to bad throughput
        if (timeout.isZero()) {
            return queue.poll();
        }
        long deadline = System.nanoTime() + timeout.toNanos();
        Task task;
        while ((task = queue.poll()) == null) {
            if (deadline <= System.nanoTime()) {
                return null;
            }
        }
        return task;
    }

    @Override
    public boolean offer(Task task) {
        return queue.offer(task);
    }

    @Override
    public boolean remove(Task task) {
        return queue.remove(task);
    }

    /**
     * Since this method is originally declared in the {@link java.util.concurrent.BlockingQueue} interface, it continuously polls tasks from the queue until it's empty.
     * Therefore, any implementing class should override this method to ensure it behaves as intended and to avoid unexpected outcomes.
     * @param dest the list to which tasks are to be transferred.
     */
    @Override
    public void drainTo(List<Runnable> dest) {
        while (!queue.isEmpty()) {
            dest.add(queue.poll());
        }
    }
}
