package org.yamikaze.unit.test.handler;

import org.yamikaze.unit.test.check.MethodDescriptor;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 15:01
 */
public interface Handler {

    /**
     * 前置处理
     * @param descriptor 单测描述
     */
    void before(MethodDescriptor descriptor);

    /**
     * 后置处理
     * @param descriptor 单测描述
     */
    void after(MethodDescriptor descriptor);

    /**
     * 异常处理
     * @param descriptor  单测描述
     * @param throwable   抛出的异常
     * @exception Throwable 抛出异常
     * @throws Throwable 抛出异常
     */
    void throwEx(MethodDescriptor descriptor, Throwable throwable) throws Throwable ;
}
