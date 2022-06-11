package org.yamikaze.unit.test.method;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-10-10 14:38
 */
public interface BaseMethodMatcher extends MethodMatcher {

    /**
     * Get base method name for current matcher
     * @return base method name.
     */
    String getBaseMethodName();
}
