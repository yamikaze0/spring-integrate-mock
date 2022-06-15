package org.yamikaze.unit.test.degrade.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-05-17 16:49
 */
public class LogDegradeExceptionHandler implements DegradeExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogDegradeExceptionHandler.class);

    private final Level level;

    public LogDegradeExceptionHandler(Level level) {
        if (level == null) {
            this.level = Level.ERROR;
        } else {
            this.level = level;
        }
    }

    @Override
    public void handler(Exception e) {
        String msg = "degrade error, e = {}";
        if (level == Level.ERROR) {
            LOGGER.error(msg, e);
        } else if (level == Level.WARN) {
            LOGGER.warn(msg, e);
        } else if (level == Level.INFO) {
            LOGGER.info(msg, e);
        } else if (level == Level.DEBUG) {
            LOGGER.debug(msg, e);
        } else if (level == Level.TRACE) {
            LOGGER.trace(msg, e);
        }
    }
}
