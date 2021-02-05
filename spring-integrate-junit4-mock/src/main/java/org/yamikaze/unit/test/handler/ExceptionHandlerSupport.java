package org.yamikaze.unit.test.handler;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-09-20 18:02
 */
public class ExceptionHandlerSupport extends HandlerSupport {

    @Override
    public void throwEx(Statement statement, Description description, Throwable throwable) throws Throwable{
        handleException(throwable);
    }

    public void handleException(Throwable throwable) throws Throwable {

    }
}
