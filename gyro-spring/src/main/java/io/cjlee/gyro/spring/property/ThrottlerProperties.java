package io.cjlee.gyro.spring.property;

import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("gyro.throttler")
public record ThrottlerProperties(Map<String, Map<String, String>> groups) {
    @ConstructorBinding
    public ThrottlerProperties {
    }

    public List<ThrottlerProperty> toProperties() {
        return groups.entrySet().stream()
                .map((entry) -> this.toProperty(entry.getKey(), entry.getValue()))
                .toList();
    }

    private ThrottlerProperty toProperty(String name, Map<String, String> property) {
        String type = property.get("type");
        if (type == null) {
            throw new IllegalArgumentException("Type doesn't exists for group " + name);
        }
        return switch (type) {
            case "one-shot", "oneshot" -> new OneShotThrottlerProperty(name, property);
            case "token-bucket", "tokenbucket" -> new TokenBucketThrottlerProperty(name, property);
            default -> throw new IllegalArgumentException("Invalid type of throttler : " + type);
        };
    }
}
