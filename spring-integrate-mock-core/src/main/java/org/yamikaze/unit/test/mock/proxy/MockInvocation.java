package org.yamikaze.unit.test.mock.proxy;

import java.lang.reflect.Method;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-11 18:48
 */
public class MockInvocation {

    /**
     * Target method.
     */
    private Method method;

    /**
     * The class with declared target method.
     */
    private Class<?> declaringClass;

    /**
     * Proxy Object.
     */
    private Object proxy;

    /**
     * Current invocation arguments.
     */
    private Object[] args;

    /**
     * The actual type of target.
     */
    private Class<?> targetClass;

    /**
     * extends field
     */
    private String beanName;

    /**
     * extends field
     *
     */
    private boolean staticInvoke;

    public boolean getStaticInvoke() {
        return staticInvoke;
    }

    public void setStaticInvoke(boolean staticInvoke) {
        this.staticInvoke = staticInvoke;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setDeclaringClass(Class<?> declaringClass) {
        this.declaringClass = declaringClass;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public void setProxy(Object proxy) {
        this.proxy = proxy;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    public Object getProxy() {
        return proxy;
    }
}
