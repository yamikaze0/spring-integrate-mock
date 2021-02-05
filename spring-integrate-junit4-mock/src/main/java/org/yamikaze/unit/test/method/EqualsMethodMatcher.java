package org.yamikaze.unit.test.method;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-10-10 14:42
 */
public class EqualsMethodMatcher implements BaseMethodMatcher {

    @Override
    public String getBaseMethodName() {
        return "equals";
    }

    @Override
    public boolean match(Method method) {
        if (method == null) {
            return false;
        }

        if (!Objects.equals(getBaseMethodName(), method.getName())) {
            return false;
        }

        if (method.getReturnType() != boolean.class) {
            return false;
        }

        if (method.getParameterCount() != 1) {
            return false;
        }

        int modifiers = method.getModifiers();

        if (!Modifier.isPublic(modifiers)) {
            return false;
        }

        if (Modifier.isStatic(modifiers)) {
            return false;
        }

        Class[] parameterTypes = method.getParameterTypes();
        return parameterTypes[0] == Object.class;

    }
}
