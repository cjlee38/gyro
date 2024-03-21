package io.cjlee.gyro.spring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.TestConstructor.AutowireMode;

@SpringBootTest
@TestConstructor(autowireMode = AutowireMode.ALL)
class ThrottleAspectTest {

    private final Target target;

    public ThrottleAspectTest(Target target) {
        this.target = target;
    }

    @Test
    void test() {
        target.oneshot();
    }
}
