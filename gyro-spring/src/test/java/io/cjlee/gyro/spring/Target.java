package io.cjlee.gyro.spring;

import org.springframework.stereotype.Component;

@Component
class Target {
    @Throttled(group = "test-oneshot")
    void oneshot() {
        System.out.println("hello world");
    }

    @Throttled(group = "test-tokenbucket")
    void tokenBucket() {
    }
}
