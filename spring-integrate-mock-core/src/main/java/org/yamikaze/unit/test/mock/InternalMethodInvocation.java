package org.yamikaze.unit.test.mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-17 09:50
 */
public class InternalMethodInvocation {

    /**
     * Current invocation target class.
     * Remove is unnecessary.
     */
    private static final ThreadLocal<Class<?>> TARGET = new InheritableThreadLocal<>();

    /**
     * 声明这个方法的类，最低层级
     */
    private Class<?> declaringClass;


    /**
     * Origin method.
     */
    private Method method;

    /**
     * invokeTimes
     */
    private final AtomicInteger realInvokedTimes = new AtomicInteger(0);
    private final AtomicInteger mockInvokedTimes = new AtomicInteger(0);
    private final List<Integer> invokedSequences = new ArrayList<>(64);

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(Class<?> declaringClass) {
        this.declaringClass = declaringClass;
    }

    void setTarget(Class<?> type) {
        TARGET.set(type);
    }

    Class<?> getTarget() {
        return TARGET.get();
    }

    public int getRealInvokedTimes() {
        return realInvokedTimes.get();
    }

    public int getMockInvokedTimes() {
        return mockInvokedTimes.get();
    }

    public InternalMethodInvocation copyOf() {
        InternalMethodInvocation mit = new InternalMethodInvocation();
        mit.declaringClass = this.declaringClass;
        mit.method = this.method;
        mit.realInvokedTimes.addAndGet(this.realInvokedTimes.get());
        mit.mockInvokedTimes.addAndGet(this.mockInvokedTimes.get());
        mit.invokedSequences.addAll(this.invokedSequences);
        return mit;
    }

    public String getKey() {
        return getTarget().getSimpleName() + "#" + method.getName() + ClassUtils.appendClasses(method.getParameterTypes(), true);
    }

    public String getSimpleKey() {
        return getTarget().getSimpleName() + "#" + method.getName();
    }

    /**
     * Incremental real-invoked times.
     */
    public void realInvoked() {
        realInvokedTimes.addAndGet(1);
        invokedSequences.add(1);
    }

    /**
     * Incremental mock-invoked times.
     */
    public void mockInvoked() {
        mockInvokedTimes.addAndGet(1);
        invokedSequences.add(0);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InternalMethodInvocation)) {
            return false;
        }

        InternalMethodInvocation mit = (InternalMethodInvocation)obj;
        return mit.declaringClass.equals(this.declaringClass)
                && mit.method.equals(this.method);
    }

    @Override
    public int hashCode() {
        return this.declaringClass.hashCode();
    }

    @Override
    public String toString() {
        return declaringClass.getName() + "#" +
                method.getName() + ClassUtils.appendClasses(method.getParameterTypes(), true) +
                " realInvokeTimes : " + realInvokedTimes.get() + " mockInvokeTimes : " + mockInvokedTimes.get() +
                Constants.LINE_SEPARATOR + " invokeSequences: (1 is realInvoke, 0 is mockInvoke)" + Constants.LINE_SEPARATOR +
                "\t" + invokedSequences;
    }

}
