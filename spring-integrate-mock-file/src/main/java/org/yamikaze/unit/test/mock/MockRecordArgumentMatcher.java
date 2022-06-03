package org.yamikaze.unit.test.mock;

import org.yamikaze.compare.CompareObjectUtils;
import org.yamikaze.compare.CompareResult;
import org.yamikaze.unit.test.mock.argument.DefaultArgumentMatcher;

import java.lang.reflect.Method;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-30 14:25
 */
public class MockRecordArgumentMatcher extends DefaultArgumentMatcher {

    private Object expectParam;

    private Method method;

    private int parameterIndex;

    public MockRecordArgumentMatcher(Object expectParam, Method method, int parameterIndex) {
        super(expectParam, method, parameterIndex);
        this.expectParam = expectParam;
        this.method = method;
        this.parameterIndex = parameterIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MockRecordArgumentMatcher)) {
            return false;
        }

        MockRecordArgumentMatcher matcher = (MockRecordArgumentMatcher)obj;

        if (method != matcher.method) {
            return false;
        }

        if (parameterIndex != matcher.parameterIndex) {
            return false;
        }

        CompareResult equalsResult = CompareObjectUtils.compare(matcher.expectParam, this.expectParam);
        return equalsResult.isSame();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
