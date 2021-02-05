package org.yamikaze.unit.test.method;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-10-10 14:45
 */
public class ToStringMethodMatcher implements BaseMethodMatcher {

    @Override
    public String getBaseMethodName() {
        return "toString";
    }

    @Override
    public boolean match(Method method) {
        if (method == null) {
            return false;
        }

        if (!Objects.equals(getBaseMethodName(), method.getName())) {
            return false;
        }

        if (method.getReturnType() != String.class) {
            return false;
        }

        if (method.getParameterCount() != 0) {
            return false;
        }

        int modifiers = method.getModifiers();

        if (!Modifier.isPublic(modifiers)) {
            return false;
        }

        return !Modifier.isStatic(modifiers);

    }
}
