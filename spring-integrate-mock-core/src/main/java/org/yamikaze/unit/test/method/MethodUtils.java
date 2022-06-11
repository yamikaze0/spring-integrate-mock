package org.yamikaze.unit.test.method;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-10-10 14:32
 */
public class MethodUtils {

    private static final Map<String, BaseMethodMatcher> BASE_MATCHERS = new HashMap<>(4);

    static {
        BASE_MATCHERS.put("hashCode", new HashcodeMethodMatcher());
        BASE_MATCHERS.put("toString", new ToStringMethodMatcher());
        BASE_MATCHERS.put("equals", new EqualsMethodMatcher());
        BASE_MATCHERS.put("clone", new CloneMethodMatcher());
    }

    /**
     * check parameter {@code method} is a base method. if {@code method} is a base method return true, otherwise return false.
     * @param method method
     * @return       if {@code method} is a base method return true, otherwise return false.
     */
    public static boolean isBaseMethod(Method method) {
        if (method == null) {
            return false;
        }

        String name = method.getName();
        BaseMethodMatcher baseMethodMatcher = BASE_MATCHERS.get(name);
        if (baseMethodMatcher == null) {
            return false;
        }

        return baseMethodMatcher.match(method);
    }
}
