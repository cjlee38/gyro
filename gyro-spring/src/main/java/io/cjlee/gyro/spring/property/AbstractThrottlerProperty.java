package io.cjlee.gyro.spring.property;

public abstract class AbstractThrottlerProperty implements ThrottlerProperty {
    private final String name;

    protected AbstractThrottlerProperty(String name) {
        this.name = name;
    }

    @Override
    public final String name() {
        return name;
    }
}
