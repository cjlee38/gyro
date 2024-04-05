package io.cjlee.gyro.task;

import java.util.concurrent.Callable;

public abstract class FutureTask<T> extends java.util.concurrent.FutureTask<T> implements Task {

    public FutureTask(Callable<T> callable) {
        super(callable);
    }

    public FutureTask(Runnable runnable, T result) {
        super(runnable, result);
    }
}
