package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.proxy.InvocationMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-17 09:50
 */
public class MethodInvokeTime {


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
    private final AtomicInteger realInvokeTimes = new AtomicInteger(0);

    private final AtomicInteger mockTimes = new AtomicInteger(0);

    private final List<Integer> invokeSequences = new ArrayList<>(64);

    private InvocationMethod currentInvocation;

    public InvocationMethod getCurrentInvocation() {
        return currentInvocation;
    }

    public void setCurrentInvocation(InvocationMethod currentInvocation) {
        this.currentInvocation = currentInvocation;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public MethodInvokeTime copyOf() {
        MethodInvokeTime mit = new MethodInvokeTime();
        mit.setDeclaringClass(declaringClass);
        mit.setMethod(method);
        mit.realInvokeTimes.addAndGet(this.realInvokeTimes.get());
        mit.mockTimes.addAndGet(this.mockTimes.get());
        mit.invokeSequences.addAll(this.invokeSequences);
        mit.currentInvocation = this.currentInvocation;

        return mit;
    }

    public String getKey() {
        return declaringClass.getSimpleName()
                + "#" + method.getName() + ClassUtils.appendClasses(method.getParameterTypes(), true);
    }

    public String getSimpleKey() {
        return declaringClass.getSimpleName()
                + "#" + method.getName();
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(Class<?> declaringClass) {
        this.declaringClass = declaringClass;
    }

    public void realInvoke() {
        realInvokeTimes.addAndGet(1);
        invokeSequences.add(1);
    }

    public void mockInvoke() {
        mockTimes.addAndGet(1);
        invokeSequences.add(0);
    }

    public int getRealInvokeTimes() {
        return realInvokeTimes.get();
    }

    public int getMockInvokeTimes() {
        return mockTimes.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MethodInvokeTime)) {
            return false;
        }

        MethodInvokeTime mit = (MethodInvokeTime)obj;
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
                " realInvokeTimes : " + realInvokeTimes.get() + " mockInvokeTimes : " + mockTimes.get() +
                Constants.LINE_SEPARATOR + " invokeSequences: (1 is realInvoke, 0 is mockInvoke)" + Constants.LINE_SEPARATOR +
                "\t" + invokeSequences;
    }

}
