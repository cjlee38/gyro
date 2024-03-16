package io.cjlee.sandevistan.ticker;

public class NativeTicker implements Ticker {
    @Override
    public long now() {
        return System.nanoTime();
    }
}
