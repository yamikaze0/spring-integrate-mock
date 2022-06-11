package org.yamikaze.unit.test.mock.argument;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-25 17:48
 */
public interface ArgumentMatcher {

    /**
     * Method argument match.
     * @param argument argument
     * @return         if match success return true, otherwise false.
     */
    boolean matchArgument(Object argument);
}
