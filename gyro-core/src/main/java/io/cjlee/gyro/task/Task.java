package io.cjlee.gyro.task;

/**
 * Represents a task that extends the capabilities of {@code Runnable} to provide enhanced control
 * over task execution sequencing, including pre- and post-execution hooks, as well as discard handling.
 * This interface allows for flexible task scheduling and execution management, making it suitable for
 * complex asynchronous processing scenarios.
 */
public interface Task extends Runnable {
    /**
     * Determines whether the task is ready for immediate execution.
     *
     * @return {@code true} if the task is ready to be executed at the time of invocation; {@code false} otherwise.
     */
    boolean runnable();

    /**
     * Registers a {@code Runnable} task to be executed prior to this task's execution. This method allows
     * for the chaining of multiple preparatory tasks, which are recommended to be non-blocking to ensure
     * efficient execution flow.
     *
     * @param previous The {@code Runnable} task to be executed before this task. It is recommended that this
     *                 task be non-blocking to avoid execution delays.
     */
    void onPrevious(Runnable previous);

    /**
     * Registers a {@code Runnable} task to be executed following the completion of this task's execution.
     * Similar to {@code onPrevious}, this method supports the chaining of multiple subsequent tasks, which
     * should ideally be non-blocking to maintain execution efficiency.
     *
     * @param next The {@code Runnable} task to be executed after this task. As with {@code onPrevious}, it is
     *             advised that this task be non-blocking.
     */
    void onNext(Runnable next);

    /**
     * Specifies a cleanup task to be executed in the event that this task is discarded. A task may be discarded
     * due to various reasons, such as the task queue reaching its capacity limit or the {@link io.cjlee.gyro.Throttler} being shut down
     * before the task could be executed.
     *
     * @param discarded The {@code Runnable} task to be executed if this task is discarded.
     */
    void onDiscarded(Runnable discarded);

    void discard(boolean mayInterruptIfRunning);
}
