package io.cjlee.gyro.queue;

import io.cjlee.gyro.task.Task;
import java.time.Duration;
import java.util.List;

/**
 * A custom interface representing a task queue, similar to {@link java.util.Queue}.
 * Tasks can be added, removed, and inspected in a first-in-first-out (FIFO) manner.
 */
public interface TaskQueue {
    /**
     * Checks whether this {@link TaskQueue} is empty.
     * @return true if the queue contains no elements, false otherwise.
     */
    boolean isEmpty();

    /**
     * Retrieves the next {@link Task} from the front of this {@link TaskQueue} without removing it.
     * This method does not throw an exception if the queue is empty.
     * @return the next task if it exists, or null otherwise.
     */
    Task peek();

    /**
     * Retrieves and removes the next {@link Task} from the front of this {@link TaskQueue}.
     * This method does not throw an exception if the queue is empty.
     * @return the next task if it exists, or null otherwise.
     */
    Task poll(Duration timeout);

    /**
     * Adds the provided {@link Task} to the end of this {@link TaskQueue}.
     * Note that this operation does not block the thread or raise exceptions.
     * @param task a wrapper class implementing either {@link Runnable} or {@link java.util.concurrent.Callable}.
     * @return true if the addition is successful, false otherwise (especially if the queue is bounded).
     */
    boolean offer(Task task);

    /**
     * Removes the specified task from this {@link TaskQueue}.
     * Equality is determined by the {@link TaskQueue} implementation and the Task's equals() & hashCode() methods.
     * @param task the task to be removed.
     * @return true if the removal is successful, false otherwise.
     */
    boolean remove(Task task);

    /**
     * Removes all tasks from the {@link TaskQueue} and transfers them to the provided {@link List} as instances of {@link Runnable}.
     * @param dest the list to which tasks are to be transferred.
     */
    void drainTo(List<Runnable> dest);
}
