package io.cjlee.gyro.ticker;

public class NativeTicker implements Ticker {
    @Override
    public long now() {
        return System.nanoTime();
    }

    @Override
    public long elapsed(long started) {
        return System.nanoTime() - started;
    }
}
