package io.cjlee.gyro.spring.property;

import io.cjlee.gyro.Throttler;
import io.cjlee.gyro.Throttlers;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.convert.DurationStyle;

public class OneShotThrottlerProperty extends AbstractThrottlerProperty implements ThrottlerProperty {
    private final Duration interval;

    public OneShotThrottlerProperty(String name, Map<String, String> property) {
        this(name, DurationStyle.detectAndParse(property.get("interval")));
    }

    public OneShotThrottlerProperty(String name, Duration interval) {
        super(name);
        this.interval = interval;
    }

    @Override
    public Throttler createThrottler() {
        return Throttlers.oneShot(interval);
    }
}
