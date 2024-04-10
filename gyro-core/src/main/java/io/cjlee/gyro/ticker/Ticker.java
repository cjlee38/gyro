package io.cjlee.gyro.ticker;

public interface Ticker {
    long now();

    long elapsed(long started);
}
