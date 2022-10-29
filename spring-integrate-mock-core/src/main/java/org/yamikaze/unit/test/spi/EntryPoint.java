package org.yamikaze.unit.test.spi;

/**
 * Used for debug.
 *
 * @author qinluo
 * @date 2022-10-19 21:06:53
 * @since 1.0.0
 */
public interface EntryPoint {

    /**
     * Execute any entry point in framework.
     *
     * @param code entry code.
     * @param args args
     */
    void execute(int code, Object[] args);

    /**
     * Self-register
     */
    default void register() {

    }
}
