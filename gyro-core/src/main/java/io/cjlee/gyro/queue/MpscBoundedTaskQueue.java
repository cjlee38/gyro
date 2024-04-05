package io.cjlee.gyro.queue;

import io.cjlee.gyro.task.Task;
import java.util.List;
import java.util.Queue;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscArrayQueue;
import org.jctools.queues.atomic.MpscAtomicArrayQueue;

public class MpscBoundedTaskQueue extends DelegateQueue implements BoundedTaskQueue {
    private final MessagePassingQueue<Task> queue;

    @SuppressWarnings("unchecked")
    public MpscBoundedTaskQueue(int capacity) {
        super(load(capacity));
        this.queue = (MessagePassingQueue<Task>) super.queue;
    }

    private static Queue<Task> load(int capacity) {
        Queue<Task> queue;
        try {
            queue = new MpscArrayQueue<>(capacity);
        } catch (Exception e) {
            queue = new MpscAtomicArrayQueue<>(capacity);
        }
        return queue;
    }

    @Override
    public void drainTo(List<Runnable> dest) {
        this.queue.drain(dest::add);
    }
}
