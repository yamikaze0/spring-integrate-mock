package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.answer.Answer;
import org.yamikaze.unit.test.mock.answer.AnswerCollector;
import org.yamikaze.unit.test.mock.answer.MockAnswerCollector;
import org.yamikaze.unit.test.mock.answer.NoopAnswer;
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
 * @date 2019-10-31 18:20
 */
public class MockUtils {

    /**
     * The No-op answer holder.
     */
    private static final ThreadLocal<Answer> NOOP_ANSWER_HOLDER = new ThreadLocal<>();

    /**
     * The proxy-map that represented class collection by this class.
     */
    private static final Map<ProxyWrapper, Class<?>> PROXY_MAP = new HashMap<>(32);

    public static <T> AnswerCollector<T> when(T result) {
        return new AnswerCollector<>();
    }

    public static <T> AnswerCollector<T> when(T result, boolean matchParams) {
        return new AnswerCollector<>(matchParams);
    }

    public static <T> T doNothing(T mockObject) {
        if (!isMock(mockObject)) {
            throw new IllegalStateException("mockObject is not proxy!");
        }

        NOOP_ANSWER_HOLDER.set(new NoopAnswer());
        return mockObject;
    }

    public static void clear() {
        NOOP_ANSWER_HOLDER.remove();
    }

    public static Answer getNoopAnswerHolder() {
        return NOOP_ANSWER_HOLDER.get();
    }


    public static MockAnswerCollector mock(Class<?> mockType) {
        return new MockAnswerCollector(mockType);
    }

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
            PROXY_MAP.put(wrapper, mockType);
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

        return PROXY_MAP.containsKey(wrapper);
    }

    public static Class<?> getMockType(Object obj) {
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

    private static boolean isCglibProxy(Class<?> proxyClass) {
        return proxyClass != null && proxyClass.getName().contains(CglibProxyFactory.PROXY_IDEN);
    }

    private static ProxyFactory determineProxyFactory(Class<?> mockType) {
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
