package io.cjlee.gyro.ticker;

public class NativeTicker implements Ticker {
    @Override
    public long now() {
        return System.nanoTime();
    }
}
