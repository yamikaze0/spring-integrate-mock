package org.yamikaze.unit.test.junit.parameterized;

import java.lang.annotation.Annotation;

/**
 * Parameter Descriptor
 *
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 16:40
 */
public class ParameterDescriptor {

    /**
     * The parameter type.
     */
    private Class<?> parameterType;

    /**
     * The annotations in parameter.
     */
    private Annotation[] parameterAnnotations;

    public ParameterDescriptor(Class<?> parameterType, Annotation[] parameterAnnotations) {
        this.parameterType = parameterType;
        this.parameterAnnotations = parameterAnnotations;
    }

    public Class<?> getParameterType() {
        return parameterType;
    }

    public void setParameterType(Class<?> parameterType) {
        this.parameterType = parameterType;
    }

    public Annotation[] getParameterAnnotations() {
        return parameterAnnotations;
    }

    public void setParameterAnnotations(Annotation[] parameterAnnotations) {
        this.parameterAnnotations = parameterAnnotations;
    }
}
