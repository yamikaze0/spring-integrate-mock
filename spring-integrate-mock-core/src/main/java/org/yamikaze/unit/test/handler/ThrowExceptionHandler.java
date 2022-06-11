package org.yamikaze.unit.test.handler;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-09-20 18:00
 */
public class ThrowExceptionHandler extends ExceptionHandlerSupport {

    @Override
    public void handleException(Throwable throwable) throws Throwable {
        throw throwable;
    }
}
