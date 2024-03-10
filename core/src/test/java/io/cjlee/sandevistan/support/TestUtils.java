package io.cjlee.sandevistan.support;

public class TestUtils {

    private TestUtils() {
    }

    public static void repeat(int count, Runnable runnable) {
        while (count-- > 0) {
            runnable.run();
        }
    }
}
