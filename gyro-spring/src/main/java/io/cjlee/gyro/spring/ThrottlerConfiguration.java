package io.cjlee.gyro.spring;

import io.cjlee.gyro.spring.property.ThrottlerProperties;
import io.cjlee.gyro.spring.property.ThrottlerProperty;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThrottlerConfiguration {

    // @ConditionalOnMissingBean
    @Bean
    public ThrottlerHandler throttlerHandler(ThrottlerProperties properties) {
        return new ThrottlerHandler(properties.toProperties()
                .stream()
                .collect(Collectors.toMap(ThrottlerProperty::name, ThrottlerProperty::createThrottler)));
    }
}
