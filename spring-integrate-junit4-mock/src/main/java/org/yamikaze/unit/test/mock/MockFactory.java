package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.proxy.CglibProxyFactory;
import org.yamikaze.unit.test.mock.proxy.JdkProxyFactory;
import org.yamikaze.unit.test.mock.proxy.ProxyFactory;
import org.yamikaze.unit.test.mock.proxy.ProxyWrapper;

import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-10-31 17:43
 */
public class MockFactory {

    private static final Map<ProxyWrapper, Class> MOCK_LIST = new HashMap<>(32);

    public static <T> T createMock(Class<T> mockType) {
        if (mockType == null) {
            throw new IllegalArgumentException("mockType must not be null!");
        }

        ProxyFactory proxyFactory = determineProxyFactory(mockType);
        T mock = null;
        if (proxyFactory != null) {
            mock = proxyFactory.createProxy(mockType);
        }

        if (mock != null) {
            ProxyWrapper wrapper = new ProxyWrapper(mockType, mock);
            MOCK_LIST.put(wrapper, mockType);
        }

        return mock;
    }

    public static boolean isMock(Object obj) {
        if (obj == null) {
            return false;
        }

        ProxyWrapper wrapper = null;
        if (Proxy.isProxyClass(obj.getClass())) {
            wrapper = new ProxyWrapper(obj.getClass().getInterfaces()[0], obj);
        }

        if (isCglibProxy(obj.getClass())) {
            wrapper = new ProxyWrapper(obj.getClass().getSuperclass(), obj);
        }

        if (wrapper == null) {
            return false;
        }

        return MOCK_LIST.containsKey(wrapper);
    }

    public static Class getMockType(Object obj) {
        if (obj == null) {
            return null;
        }

        if (Proxy.isProxyClass(obj.getClass())) {
            return obj.getClass().getInterfaces()[0];
        }

        if (isCglibProxy(obj.getClass())) {
            return obj.getClass().getSuperclass();
        }

        return null;

    }

    private static boolean isCglibProxy(Class proxyClass) {
        return proxyClass != null && proxyClass.getName().contains(CglibProxyFactory.PROXY_IDEN);
    }

    private static ProxyFactory determineProxyFactory(Class mockType) {
        boolean useJdkProxy = mockType.isInterface();
        if (useJdkProxy) {
            return JdkProxyFactory.getProxyFactory();
        }

        int modifiers = mockType.getModifiers();
        if (Modifier.isFinal(modifiers)) {
            throw new IllegalStateException("final class can't be subclass, mockType = " + mockType.getName());
        }

        if (Modifier.isAbstract(modifiers)) {
            throw new IllegalStateException("abstract class can't be subclass, mockType = " + mockType.getName());
        }

        return CglibProxyFactory.getProxyFactory();
    }
}
