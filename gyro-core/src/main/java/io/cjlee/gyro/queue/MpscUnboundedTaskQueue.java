package io.cjlee.gyro.queue;

import io.cjlee.gyro.task.Task;
import java.util.List;
import java.util.Queue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jctools.queues.atomic.MpscUnboundedAtomicArrayQueue;

public class MpscUnboundedTaskQueue implements UnboundedTaskQueue {
    private final Queue<Task> queue;

    public MpscUnboundedTaskQueue(int capacity) {
        Queue<Task> queue;
        try {
            queue = new MpscUnboundedArrayQueue<>(capacity);
        } catch (Exception e) {
            e.printStackTrace();
            queue = new MpscUnboundedAtomicArrayQueue<>(capacity);
        }
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
    public Task poll() {
        return queue.poll();
    }

    @Override
    public boolean offer(Task task) {
        return queue.offer(task);
    }

    @Override
    public boolean remove(Task task) {
        return queue.remove(task);
    }

    @Override
    public void drainTo(List<Runnable> to) {
        // TODO : may lead to unexpected result.
        while (!queue.isEmpty()) {
            to.add(queue.poll());
        }
    }
}