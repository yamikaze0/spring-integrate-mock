package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.annotation.MockEnhance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-09-15 20:28
 */
public class MockRunnerHelper {

    public static List<Class<?>> extraEnhanceClasses(MockEnhance me) {
        List<Class<?>> enhanceClasses = new ArrayList<>(16);

        if (me != null && me.value().length > 0) {
            addAll(enhanceClasses, me.value());
        }

        return enhanceClasses.stream().distinct().collect(Collectors.toList());
    }

    /**
     * cs:off
     */
    private static void addAll(List<Class<?>> classes, Class<?>[] ca) {
        classes.addAll(Arrays.asList(ca));
    }
}
