package io.cjlee.gyro.spring.property;

import io.cjlee.gyro.Throttler;
import io.cjlee.gyro.Throttlers;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.convert.DurationStyle;

public class TokenBucketThrottlerProperty extends AbstractThrottlerProperty implements ThrottlerProperty {
    private final int capacity;
    private final int replenishAmount;
    private final Duration replenishDelay;

    public TokenBucketThrottlerProperty(String name, Map<String, String> property) {
        this(name,
                Integer.parseInt(property.get("capacity")),
                Integer.parseInt(property.get("replenishAmount")),
                DurationStyle.detectAndParse(property.get("replenishDelay")));
    }

    public TokenBucketThrottlerProperty(String name, int capacity, int replenishAmount, Duration replenishDelay) {
        super(name);
        this.capacity = capacity;
        this.replenishAmount = replenishAmount;
        this.replenishDelay = replenishDelay;
    }

    @Override
    public Throttler createThrottler() {
        return Throttlers.tokenBucket(capacity, replenishAmount, replenishDelay);
    }
}
