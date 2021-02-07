package org.yamikaze.unit.test.junit.parameterized.converter;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 18:09
 */
public class ShortParamConverter implements ParamConverter {

    private final boolean isPrimitive;

    public ShortParamConverter(boolean isPrimitive) {
        this.isPrimitive = isPrimitive;
    }

    @Override
    public Object convert(String val) throws Throwable {
        if (val == null || val.trim().length() == 0) {

            if (isPrimitive) {
                return (short)0;
            }
            return null;
        }

        return Short.valueOf(val.trim());
    }
}
