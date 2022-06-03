package org.yamikaze.unit.test.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mock场景录制
 *
 * @author 三刀
 * @version V1.0 , 2019/10/31
 */
public class MockRecord {
    static List anyList = new ArrayList();
    /**
     * 构造接口结果
     */
    Map<String, Object> recordMap = new HashMap<>();
    /**
     * 构造接口异常
     */
    Map<String, Throwable> recordThrowableMap = new HashMap<>();
    /**
     * 断言接口结果
     */
    Map<String, AssertFunction<Object>> recordAssertMap = new HashMap<>();
    /**
     * 注入接口入参
     */
    Map<String, Object[]> recordArgsMap = new HashMap<>();
    /**
     * 当然录制的接口
     */
    String currentMockMethod;

    /**
     * 当然注入的入参
     */
    Object[] currentArgs;

    public static List anyList() {
        return anyList;
    }

    public static <T> T any() {
        return null;
    }

    static String getMockKey(Class clazz, Method method) {
        return clazz.getName() + method.getName() + ClassUtils.appendClasses(method.getParameterTypes(), false);
    }

    public <T> T mock(Class<T> clazz) {
        JDKDynamicProxy proxy = new JDKDynamicProxy(clazz);
        return proxy.getProxy(clazz);
    }

    Throwable getThrowable(String key) {
        return recordThrowableMap.get(key);
    }

    public <P> MockReturn<P> when(P t) {
        return new MockReturn<P>(this);
    }

    /**
     * 重新定义入参
     *
     * @param t
     * @param <P>
     * @return
     */
    public <P> MockRecord replaceArgs(P t) {
        recordArgsMap.put(currentMockMethod, currentArgs);
        currentArgs = null;
        return this;
    }

    private class JDKDynamicProxy implements InvocationHandler {
        private Class clazz;

        JDKDynamicProxy(Class clazz) {
            this.clazz = clazz;
        }

        /**
         * 获取被代理接口实例对象
         *
         * @param <T>
         * @return
         */
        public <T> T getProxy(Class<T> clazz) {
            return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            System.out.println("Do something before");
//            Object result = method.invoke(target, args);
            currentMockMethod = getMockKey(clazz, method);
            currentArgs = args;
//            System.out.println("Do something after");
//            recordArgsMap
            return null;
        }
    }
}

