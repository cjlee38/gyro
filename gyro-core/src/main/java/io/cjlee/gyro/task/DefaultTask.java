package io.cjlee.gyro.task;

import java.util.concurrent.Callable;

public class DefaultTask<T> extends FutureTask<T> implements Task {
    private Runnable previous;
    private Runnable next;

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

    @Override
    public void onPrevious(Runnable previous) {
        this.previous = previous;
    }

    @Override
    public void onNext(Runnable next) {
        this.next = next;
    }

    @Override
    public void run() {
        if (previous != null) {
            previous.run();
        }
        super.run();
        if (next != null) {
            next.run();
        }
    }
}
