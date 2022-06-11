package org.yamikaze.unit.test.mock.proxy;

import java.lang.reflect.Proxy;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-10-31 17:46
 */
public class JdkProxyFactory implements ProxyFactory {

    private static final JdkProxyFactory JDK_PROXY_FACTORY = new JdkProxyFactory();

    public static ProxyFactory getProxyFactory() {
        return JDK_PROXY_FACTORY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> clz) {
        if (clz == null) {
            throw new IllegalArgumentException("proxy clz must not be null!");
        }

        if (!clz.isInterface()) {
            throw new IllegalArgumentException("proxy clz [" + clz.getName() + "] must be an interface!");
        }

        return (T)Proxy.newProxyInstance(clz.getClassLoader(), new Class[] {clz}, new JdkInvocationHandler());
    }
}
