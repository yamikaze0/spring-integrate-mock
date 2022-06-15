package org.yamikaze.unit.test.mock.argument;


import org.yamikaze.compare.CompareObjectUtils;
import org.yamikaze.compare.CompareResult;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-26 10:57
 */
public class DefaultArgumentMatcher implements ArgumentMatcher {

    private Object expectParam;

    private Method method;

    private int parameterIndex;

    public DefaultArgumentMatcher() {
    }

    public DefaultArgumentMatcher(Object expectParam, Method method, int parameterIndex) {
        this.expectParam = expectParam;
        this.method = method;
        this.parameterIndex = parameterIndex;
    }

    @Override
    public boolean matchArgument(Object argument) {
        boolean lastArgument = (method.getParameterCount() - 1) == parameterIndex;
        if (!lastArgument || !method.isVarArgs()) {
            if (expectParam == argument) {
                return true;
            }
            CompareResult equalsResult = CompareObjectUtils.compare(expectParam, argument);
            return equalsResult.isSame();
        }

        List<Object> expectParams = unpackAsList(expectParam);
        List<Object> actualParams = unpackAsList(argument);
        if (expectParams.size() != actualParams.size()) {
            return false;
        }

        for (int index = 0, size = actualParams.size(); index < size; index++) {
            if (Objects.equals(actualParams.get(index), expectParams.get(index))) {
                continue;
            }

            CompareResult equalsResult = CompareObjectUtils.compare(actualParams.get(index), expectParams.get(index));
            if (!equalsResult.isSame()) {
                return false;
            }
        }


        return true;
    }

    /**
     * cs:off
     */
    private List<Object> unpackAsList(Object varArgArray) {
        if (varArgArray instanceof Object[]) {
            return Arrays.asList((Object[]) varArgArray);
        } else if (varArgArray.getClass().isArray()) {
            Object[] primitiveArray = new Object[Array.getLength(varArgArray)];
            for (int i = 0; i < primitiveArray.length; i++) {
                primitiveArray[i] = Array.get(varArgArray, i);
            }
            return Arrays.asList(primitiveArray);
        } else {
            return Collections.singletonList(varArgArray);
        }
    }
}
