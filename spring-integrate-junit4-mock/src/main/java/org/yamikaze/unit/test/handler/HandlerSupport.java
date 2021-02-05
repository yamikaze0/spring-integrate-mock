package org.yamikaze.unit.test.handler;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 15:07
 */
public class HandlerSupport implements Handler {

    @Override
    public void before(Statement statement, Description description) {

    }

    @Override
    public void after(Statement statement, Description description) {

    }

    @Override
    public void throwEx(Statement statement, Description description, Throwable throwable) throws Throwable{

    }
}
