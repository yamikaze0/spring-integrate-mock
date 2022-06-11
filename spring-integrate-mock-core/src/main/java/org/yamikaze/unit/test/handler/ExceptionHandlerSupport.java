package org.yamikaze.unit.test.handler;

import org.yamikaze.unit.test.check.MethodDescriptor;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-09-20 18:02
 */
public class ExceptionHandlerSupport extends HandlerSupport {

    @Override
    public void throwEx(MethodDescriptor descriptor, Throwable throwable) throws Throwable{
        handleException(throwable);
    }

    public void handleException(Throwable throwable) throws Throwable {

    }
}
