package org.yamikaze.unit.test.method;

import java.lang.reflect.Method;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-10-10 14:36
 */
public interface MethodMatcher {

    /**
     * The {@code method} is matched.
     * @param method method
     * @return       if method matched return true, otherwise return false.
     */
    boolean match(Method method);
}
