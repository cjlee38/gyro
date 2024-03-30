package io.cjlee.gyro.utils;

import java.util.List;

public class ListUtils {
    private ListUtils() {
    }

    public static <T> T first(List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalStateException("Cannot obtain first element because list is empty");
        }
        return list.get(0);
    }

    public static <T> T last(List<T> list) {
        if (list.isEmpty()) {
            throw new IllegalStateException("Cannot obtain last element because list is empty");
        }
        return list.get(list.size() - 1);
    }
}
