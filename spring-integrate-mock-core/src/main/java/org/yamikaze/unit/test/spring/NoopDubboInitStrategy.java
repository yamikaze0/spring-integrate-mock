package org.yamikaze.unit.test.spring;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author qinluo
 * @date 2022-07-08 21:01:17
 * @since 1.0.0
 */
public class NoopDubboInitStrategy implements DubboInitStrategy {

    @Override
    public <T> FactoryBean<T> wrap(String name, FactoryBean<T> origin) {
        return origin;
    }
}
