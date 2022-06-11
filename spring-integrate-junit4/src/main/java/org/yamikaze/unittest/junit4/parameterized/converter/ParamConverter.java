package org.yamikaze.unittest.junit4.parameterized.converter;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 17:49
 */
public interface ParamConverter {

    /**
     * Convert string val to Object.
     *
     * @param val string val.
     * @return    actual object.
     * @throws Throwable occurred error during convert
     */
    Object convert(String val) throws Throwable;
}
