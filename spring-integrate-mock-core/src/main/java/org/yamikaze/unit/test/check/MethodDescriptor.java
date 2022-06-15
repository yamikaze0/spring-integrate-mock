package org.yamikaze.unit.test.check;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author qinluo
 * @date 2022-06-11 21:33:55
 * @since 1.0.0
 */
public class MethodDescriptor {

    /**
     * class
     */
    private Class<?> type;

    /**
     * method's name
     */
    private String methodName;

    private Method method;

    private Annotation[] fAnnotations;

    public void setfAnnotations(Annotation[] fAnnotations) {
        this.fAnnotations = fAnnotations;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getClassName() {
        return type.getName();
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        if (method != null) {
            return method.getAnnotation(annotationType);
        }

        for (Annotation each : fAnnotations) {
            if (each.annotationType().equals(annotationType)) {
                return annotationType.cast(each);
            }
        }
        return null;
    }

}
