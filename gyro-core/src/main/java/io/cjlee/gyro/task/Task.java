package io.cjlee.gyro.task;

public interface Task extends Runnable {
    boolean runnable();

    void onPrevious(Runnable previous);

    void onNext(Runnable next);
}
