package org.yamikaze.unit.test.degrade;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-07-30 15:10
 */
public abstract class AbstractDegradeExecutor<T> implements DegradeExecutor<T> {

    private final Class<T> type;

    @SuppressWarnings("unchecked")
    public AbstractDegradeExecutor() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
        type = (Class<T>)parameterizedType.getActualTypeArguments()[0];
    }

    public final Class<T> getType() {
        return type;
    }
}
