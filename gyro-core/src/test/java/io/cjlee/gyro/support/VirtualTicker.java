package io.cjlee.gyro.support;

import io.cjlee.gyro.ticker.Ticker;

public class VirtualTicker implements Ticker {
    @Override
    public long now() {
        return 0;
    }
}
