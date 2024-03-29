package org.yamikaze.unit.test.mock;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 17:32
 */
public class MockException extends RuntimeException {

    private static final long serialVersionUID = 7593151027940954345L;

    public MockException(String message) {
        super(message);
    }

    public MockException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
