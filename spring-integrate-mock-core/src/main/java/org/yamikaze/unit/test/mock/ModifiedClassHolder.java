package org.yamikaze.unit.test.mock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinluo
 * @date 2022-06-11 22:34:30
 * @since 1.0.0
 */
public class ModifiedClassHolder {

    private static final Map<String, Class<?>> MODIFIED_CLASSES = new ConcurrentHashMap<>();

    public static Class<?> get(String className) {
        return MODIFIED_CLASSES.get(className);
    }

    public static void put(String classname, Class<?> type) {
        MODIFIED_CLASSES.put(classname, type);
    }

    public static boolean exist(String classname) {
        return get(classname) != null;
    }
}
