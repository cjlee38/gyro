package io.cjlee.gyro.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DefaultTask<T> extends FutureTask<T> implements Task {
    private final List<Runnable> previous = new ArrayList<>();
    private final List<Runnable> next = new ArrayList<>();

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
        this.previous.add(previous);
    }

    @Override
    public void onNext(Runnable next) {
        this.next.add(next);
    }

    @Override
    public void run() {
        if (!previous.isEmpty()) {
            previous.forEach(Runnable::run);
        }
        super.run();
        if (!next.isEmpty()) {
            next.forEach(Runnable::run);
        }
    }
}
