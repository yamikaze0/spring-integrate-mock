package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.tree.Profilers;

/**
 * @author qinluo
 * @date 2022-07-07 22:56:22
 * @since 1.0.0
 */
public class MockContextUtils {

    public static void prepare() {
        Profilers.enable();
        MethodMockInterceptor.clear();
        DataCodeFactory.clear();
    }

    public static void clear() {
        Profilers.disable();
        RecordBehaviorList.INSTANCE.clear();
        DataCodeFactory.clear();
    }
}
