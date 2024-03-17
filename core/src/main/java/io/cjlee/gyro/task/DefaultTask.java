package io.cjlee.gyro.task;

public class DefaultTask implements Task {
    private final Runnable runnable;

    public DefaultTask(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
    }

    @Override
    public boolean runnable() {
        return true;
    }
}
