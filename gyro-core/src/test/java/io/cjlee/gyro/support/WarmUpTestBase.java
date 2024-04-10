package io.cjlee.gyro.support;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.commons.support.ReflectionSupport;

public class WarmUpTestBase {
    @BeforeAll
    static void warmUp() {
        loadClass();
    }

    private static void loadClass() {
        List<Class<?>> classes = ReflectionSupport.findAllClassesInPackage("io.cjlee.gyro", __ -> true, __ -> true);
        classes.forEach(it -> {
            try {
                String name = it.getCanonicalName();
                if (name == null || "null".equals(name)) {
                    return;
                }
                Class.forName(name);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
