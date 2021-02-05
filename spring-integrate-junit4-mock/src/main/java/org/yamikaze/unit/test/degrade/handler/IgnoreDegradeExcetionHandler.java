package org.yamikaze.unit.test.degrade.handler;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-05-17 17:09
 */
public class IgnoreDegradeExcetionHandler implements DegradeExceptionHandler {

    @Override
    public void handler(Exception e) {
        //ignore exception.
    }
}
