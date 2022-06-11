package org.yamikaze.unittest.junit4.parameterized.converter;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 18:09
 */
public class StringParamConverter implements ParamConverter {

    @Override
    public Object convert(String val) {
        return val;
    }
}
