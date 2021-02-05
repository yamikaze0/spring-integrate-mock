package org.yamikaze.unit.test.degrade.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-12 14:37
 */
public class TimeoutExceptionHandler implements DegradeExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutExceptionHandler.class);

    @Override
    public void handler(Exception e) {
        LOGGER.error("degrade timeoutException error, e = {}", e);
    }
}
