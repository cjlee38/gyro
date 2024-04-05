package io.cjlee.gyro.queue;

import io.cjlee.gyro.task.Task;
import java.util.List;
import java.util.Queue;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedArrayQueue;
import org.jctools.queues.atomic.MpscUnboundedAtomicArrayQueue;

public class MpscUnboundedTaskQueue extends DelegateQueue implements UnboundedTaskQueue {
    private MessagePassingQueue<Task> queue;

    @SuppressWarnings("unchecked")
    public MpscUnboundedTaskQueue(int capacity) {
        super(load(capacity));
        this.queue = (MessagePassingQueue<Task>) super.queue;
    }

    private static Queue<Task> load(int capacity) {
        Queue<Task> queue;
        try {
            queue = new MpscUnboundedArrayQueue<>(capacity);
        } catch (Exception e) {
            queue = new MpscUnboundedAtomicArrayQueue<>(capacity);
        }
        return queue;
    }

    @Override
    public void drainTo(List<Runnable> dest) {
        this.queue.drain(dest::add);
    }
}
