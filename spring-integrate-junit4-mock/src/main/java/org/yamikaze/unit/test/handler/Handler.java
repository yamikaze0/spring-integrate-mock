package org.yamikaze.unit.test.handler;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 15:01
 */
public interface Handler {

    /**
     * 前置处理
     * @param statement   执行声明
     * @param description 单测描述
     */
    void before(Statement statement, Description description);

    /**
     * 后置处理
     * @param statement   执行声明
     * @param description 单测描述
     */
    void after(Statement statement, Description description);

    /**
     * 异常处理
     * @param statement   执行声明
     * @param description 单测描述
     * @param throwable   抛出的异常
     * @exception Throwable 抛出异常
     * @throws Throwable 抛出异常
     */
    void throwEx(Statement statement, Description description, Throwable throwable) throws Throwable ;
}
