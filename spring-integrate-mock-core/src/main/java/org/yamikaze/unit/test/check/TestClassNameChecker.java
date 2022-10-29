package org.yamikaze.unit.test.check;

import java.util.regex.Pattern;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 14:23
 */
public class TestClassNameChecker implements Checker {

    private static final Pattern PATTERN = Pattern.compile("[a-zA-Z0-9]+");

    @Override
    public void check(MethodDescriptor descriptor) {
        if (!ENABLED) {
            return;
        }

        String className = descriptor.getType().getName();

        //description.getClassName得到的是full name
        className = className.substring(className.lastIndexOf(".") + 1);

        if (!className.startsWith(CLASS_TEST_PREFIX) && !className.endsWith(CLASS_TEST_SUFFIX)) {
            throw new IllegalStateException("test class name must start with Test or end with Test, but actually is " + className);
        }

        if (className.startsWith(CLASS_TEST_PREFIX) && className.endsWith(CLASS_TEST_SUFFIX)) {
            throw new IllegalStateException("test class name must not start with Test or end with Test, but actually is " + className);
        }

        if (!PATTERN.matcher(className).matches()) {
            throw new IllegalStateException("test class name must match pattern [a-zA-Z0-9]+, but actually is " + className);

        }
    }
}
