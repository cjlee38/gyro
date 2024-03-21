package io.cjlee.gyro.spring;

import io.cjlee.gyro.spring.property.ThrottlerProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;

@SpringBootTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class ThrottlerPropertiesTest {

    private final ThrottlerProperties properties;

    ThrottlerPropertiesTest(ThrottlerProperties properties) {
        this.properties = properties;
    }

    @Test
    void bind() {
        System.out.println("hello");
        System.out.println("properties = " + properties);
    }
}
