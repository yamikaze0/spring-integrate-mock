package org.yamikaze.unit.test.degrade;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-11 11:14
 */
public class DegradeException extends RuntimeException {

    private static final long serialVersionUID = -8358419232427492532L;

    public DegradeException() {
    }

    public DegradeException(String message) {
        super(message);
    }

    public DegradeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DegradeException(Throwable cause) {
        super(cause);
    }

    public DegradeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
