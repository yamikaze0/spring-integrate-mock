package org.yamikaze.unit.test.mock.proxy;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-10-31 17:45
 */
public interface ProxyFactory {

    /**
     * Create Proxy object
     * @param clz class
     * @param <T> generic type
     * @return    proxy
     */
    <T> T createProxy(Class<T> clz);
}
