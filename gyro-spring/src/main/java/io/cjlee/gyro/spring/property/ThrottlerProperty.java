package io.cjlee.gyro.spring.property;

import io.cjlee.gyro.Throttler;

public interface ThrottlerProperty {
    String name();
    Throttler createThrottler(); // TODO : possible to inject executor service bean
}
