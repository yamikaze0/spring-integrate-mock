package org.yamikaze.unit.test.mock.answer;

import org.yamikaze.unit.test.mock.proxy.InvocationMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-10-31 17:58
 */
public class DefaultValueAnswer extends AbstractAnswer {

    private static final Map<Class<?>, Object> DEFAULT_VALUE = new HashMap<>();


    static {
        DEFAULT_VALUE.put(boolean.class, false);
        DEFAULT_VALUE.put(byte.class, (byte)0);
        DEFAULT_VALUE.put(short.class, (short)0);
        DEFAULT_VALUE.put(int.class, 0);
        DEFAULT_VALUE.put(long.class, 0L);
        DEFAULT_VALUE.put(float.class, 0.0f);
        DEFAULT_VALUE.put(double.class, 0D);
        DEFAULT_VALUE.put(char.class, '0');

        DEFAULT_VALUE.put(Boolean.class, false);
        DEFAULT_VALUE.put(Byte.class, (byte)0);
        DEFAULT_VALUE.put(Short.class, (short)0);
        DEFAULT_VALUE.put(Integer.class, 0);
        DEFAULT_VALUE.put(Long.class, 0L);
        DEFAULT_VALUE.put(Float.class, 0.0f);
        DEFAULT_VALUE.put(Double.class, 0D);
        DEFAULT_VALUE.put(Character.class, '0');
    }

    @Override
    public Object answer(InvocationMethod invocation) {
        this.accessed = true;

        Method method = invocation.getMethod();
        Class<?> returnType = method.getReturnType();
        return DEFAULT_VALUE.get(returnType);
    }

    @Override
    public String toString() {
        return "DefaultValueAnswer { } ";
    }
}
