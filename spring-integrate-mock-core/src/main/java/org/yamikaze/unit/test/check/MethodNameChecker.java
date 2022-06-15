package org.yamikaze.unit.test.check;

import java.util.regex.Pattern;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 14:10
 */
public class MethodNameChecker implements Checker {

    private static final Pattern PATTERN = Pattern.compile("[a-zA-Z0-9]+");

    @Override
    public void check(MethodDescriptor description) {
        String methodName = description.getMethodName();
        if (!methodName.startsWith(METHOD_TEST_PREFIX)) {
            throw new IllegalStateException("test method name must start with test, but actually is " + methodName);
        }

        String actuallyMethodName = methodName.substring(METHOD_TEST_PREFIX.length());
        boolean matches = PATTERN.matcher(actuallyMethodName).matches();
        if (!matches) {
            throw new IllegalStateException("test method name must match pattern [a-zA-z0-9]+, but actually is " + methodName);
        }
    }
}
