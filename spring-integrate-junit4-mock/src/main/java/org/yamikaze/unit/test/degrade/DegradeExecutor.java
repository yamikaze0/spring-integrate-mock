package org.yamikaze.unit.test.degrade;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-05-17 16:45
 */
@FunctionalInterface
public interface DegradeExecutor<T> {

    /**
     * 执行逻辑
     * @return 出参
     */
    T execute();
}
