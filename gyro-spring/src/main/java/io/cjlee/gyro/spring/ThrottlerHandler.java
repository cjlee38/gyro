package io.cjlee.gyro.spring;

import io.cjlee.gyro.Throttler;
import java.util.Map;
import java.util.Objects;

public class ThrottlerHandler {

    private final Map<String, Throttler> throttlers;

    public ThrottlerHandler(Map<String, Throttler> throttlers) {
        Objects.requireNonNull(throttlers, "throttlers should not be null");
        this.throttlers = throttlers;
    }

    public void handle(String group, Runnable runnable) {
        Throttler throttler = throttlers.get(group);
        throttler.submit(runnable);
    }
}
