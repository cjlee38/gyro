package io.cjlee.gyro;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * The {@code Throttler} interface defines the mechanism for submitting tasks
 * for execution with rate limiting. It supports asynchronous execution of
 * {@link Runnable} and {@link Callable} tasks, ensuring that tasks are executed
 * at an appropriate time based on the specified throttling strategy.
 * This interface also provides methods for controlled shutdown of task execution,
 * allowing for graceful termination.
 */
public interface Throttler {

    /**
     * Submits a {@link Runnable} task for execution at an appropriate time
     * determined by the throttling strategy. This method returns a {@link Future}
     * representing the pending completion of the task.
     *
     * @param task the {@link Runnable} task to be executed
     * @return a {@link Future} representing the pending completion of the task
     */
    Future<?> submit(Runnable task);

    /**
     * Submits a {@link Callable} task for execution, allowing tasks that return
     * a result. The task is executed at an appropriate time determined by the
     * throttling strategy. This method returns a {@link Future} representing
     * the pending result of the task.
     *
     * @param <T> the type of the task's result
     * @param task the {@link Callable} task to be executed
     * @return a {@link Future} representing the pending result of the task
     */
    <T> Future<T> submit(Callable<T> task);

    /**
     * Initiates an orderly shutdown in which tasks that have been submitted
     * are executed within the specified duration before shutting down. New tasks
     * cannot be submitted once this method is called. This method does not wait
     * for previously submitted tasks to complete execution.
     *
     * @param duration the duration to wait before terminating
     */
    void shutdown(Duration duration);

    /**
     * Attempts to stop all actively executing tasks, halts the processing of waiting
     * tasks, and returns a list of the tasks that were awaiting execution.
     * This method attempts to terminate tasks immediately, without waiting for
     * actively executing tasks to terminate.
     *
     * @return a list of tasks that were awaiting execution
     */
    List<Runnable> shutdownNow();
}
