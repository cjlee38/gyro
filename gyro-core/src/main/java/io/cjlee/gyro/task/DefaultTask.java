package io.cjlee.gyro.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class DefaultTask<T> extends FutureTask<T> implements Task {
    private final List<Runnable> previous = new ArrayList<>();
    private final List<Runnable> next = new ArrayList<>();
    private final List<Runnable> cleanUp = new ArrayList<>();

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
        if (previous == null) {
            return;
        }
        this.previous.add(previous);
    }

    @Override
    public void onNext(Runnable next) {
        if (next == null) {
            return;
        }
        this.next.add(next);
    }

    @Override
    public void onDiscarded(Runnable discarded) {
        if (discarded == null) {
            return;
        }
        this.cleanUp.add(discarded);
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

    @Override
    public void discard(boolean mayInterruptIfRunning) {
        super.cancel(mayInterruptIfRunning);
        cleanUp.forEach(Runnable::run);
    }
}
