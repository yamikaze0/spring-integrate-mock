package org.yamikaze.unittest.junit4.parameterized.converter;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 18:09
 */
public class BooleanParamConverter implements ParamConverter {

    private final boolean isPrimitive;

    public BooleanParamConverter(boolean isPrimitive) {
        this.isPrimitive = isPrimitive;
    }

    @Override
    public Object convert(String val) throws Throwable {
        if (val == null || val.trim().length() == 0) {

            if (isPrimitive) {
                return false;
            }

            return null;
        }

        return Boolean.parseBoolean(val.trim());
    }
}
