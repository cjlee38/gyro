package io.cjlee.gyro.task;

import java.util.concurrent.Callable;

public class DefaultTask<T> extends FutureTask<T> implements Task {
    public DefaultTask(Runnable runnable) {
        super(runnable, null);
    }

    public DefaultTask(Callable<T> callable) {
        super(callable);
    }

    @Override
    public boolean runnable() {
        return true;
    }
}
