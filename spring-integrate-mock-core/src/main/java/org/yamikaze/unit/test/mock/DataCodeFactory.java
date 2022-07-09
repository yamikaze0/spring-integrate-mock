package org.yamikaze.unit.test.mock;

import java.util.HashMap;
import java.util.Map;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-11 16:25
 */
public class DataCodeFactory {

    private static final Map<String, Object> DATA_MAP = new HashMap<>(32);

    public static Object getData(String code) {
        if (code == null) {
            throw new IllegalStateException("data code must not be null!");
        }

        return DATA_MAP.get(code);
    }

    public static boolean contains(String code) {
        return DATA_MAP.containsKey(code);
    }

    public static void register(String code, Object e) {
        if (code == null) {
            throw new IllegalStateException("data code must not be null!");
        }

        //data can null
        DATA_MAP.put(code, e);
    }

    public static void clear() {
        DATA_MAP.clear();
    }
}
