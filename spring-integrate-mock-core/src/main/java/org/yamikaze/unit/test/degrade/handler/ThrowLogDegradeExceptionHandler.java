package org.yamikaze.unit.test.degrade.handler;

import org.yamikaze.unit.test.degrade.DegradeException;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-05-17 16:50
 */
public class ThrowLogDegradeExceptionHandler implements DegradeExceptionHandler {

    @Override
    public void handler(Exception e) {
        throw new DegradeException("degrade exception", e);
    }
}
