package org.yamikaze.unit.test.degrade.handler;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-05-17 16:47
 */
public interface DegradeExceptionHandler {

    /**
     * 处理异常
     * @param e 异常对象
     */
    void handler(Exception e);
}
