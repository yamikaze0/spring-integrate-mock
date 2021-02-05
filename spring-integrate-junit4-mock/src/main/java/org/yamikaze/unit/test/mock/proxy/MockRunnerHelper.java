package org.yamikaze.unit.test.mock.proxy;

import org.yamikaze.unit.test.mock.annotation.MockEnhance;
import org.yamikaze.unit.test.mock.annotation.MockFinal;
import org.yamikaze.unit.test.mock.annotation.MockStatic;

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

    public static List<Class> extraEnhanceClasses(MockStatic ms, MockFinal mf, MockEnhance me) {
        List<Class> enhanceClasses = new ArrayList<>(16);

        if (ms != null && ms.value().length > 0) {
            addAll(enhanceClasses, ms.value());
        }


        if (mf != null && mf.value().length > 0) {
            addAll(enhanceClasses, mf.value());
        }


        if (me != null && me.value().length > 0) {
            addAll(enhanceClasses, me.value());
        }

        return enhanceClasses.stream().distinct().collect(Collectors.toList());
    }

    /**
     * cs:off
     */
    private static void addAll(List<Class> classes, Class[] ca) {
        classes.addAll(Arrays.asList(ca));
    }
}
