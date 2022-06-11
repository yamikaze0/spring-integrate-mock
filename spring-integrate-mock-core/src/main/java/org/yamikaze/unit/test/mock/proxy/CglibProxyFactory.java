package org.yamikaze.unit.test.mock.proxy;

import org.springframework.cglib.core.NamingPolicy;
import org.springframework.cglib.proxy.Enhancer;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-11 19:51
 */
public class CglibProxyFactory implements ProxyFactory {

    private static final CglibProxyFactory INSTANCE = new CglibProxyFactory();

    public static final String PROXY_IDEN = "ByYtMockitoCGLIB";

    private static final NamingPolicy NAMING_POLICY = (prefix, source, key, names) -> {
        if (prefix == null) {
            prefix = "org.springframework.cglib.empty.Object";
        } else if (prefix.startsWith("java")) {
            prefix = "$" + prefix;
        }

        String base = prefix + "$$" + source.substring(source.lastIndexOf(46) + 1) + PROXY_IDEN + "$$" + Integer.toHexString(key.hashCode());
        String attempt = base;

        int var7 = 2;
        while (names.evaluate(attempt)) {
            attempt = base + "_" + var7++;
        }

        return attempt;
    };

    public static ProxyFactory getProxyFactory() {
        return INSTANCE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> clz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clz);
        enhancer.setCallback(new CglibMethodInterceptor());
        enhancer.setNamingPolicy(NAMING_POLICY);

        Object proxyObject = enhancer.create();

        return (T)proxyObject;
    }
}
