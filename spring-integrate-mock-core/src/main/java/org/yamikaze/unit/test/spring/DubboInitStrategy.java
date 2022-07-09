package org.yamikaze.unit.test.spring;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author qinluo
 * @date 2022-07-08 20:47:24
 * @since 1.0.0
 */
public interface DubboInitStrategy {

    /**
     * Wrap dubbo reference bean.
     *
     * @param name   name
     * @param origin origin factory bean
     * @param <T>    row type
     * @return       after wrapped bean
     */
    <T> FactoryBean<T> wrap(String name, FactoryBean<T> origin);
}
