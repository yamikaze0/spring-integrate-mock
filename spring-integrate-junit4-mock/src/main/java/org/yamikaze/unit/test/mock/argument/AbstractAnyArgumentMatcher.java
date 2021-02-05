package org.yamikaze.unit.test.mock.argument;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-25 17:58
 */
public abstract class AbstractAnyArgumentMatcher<T> implements ArgumentMatcher {

    private Class<T> specificClass;

    @SuppressWarnings("unchecked")
    public AbstractAnyArgumentMatcher() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        Type type = parameterizedType.getActualTypeArguments()[0];
        specificClass = (Class) type;
    }

    @Override
    public boolean matchArgument(Object argument) {
        return argument == null || specificClass.isAssignableFrom(argument.getClass());
    }
}
