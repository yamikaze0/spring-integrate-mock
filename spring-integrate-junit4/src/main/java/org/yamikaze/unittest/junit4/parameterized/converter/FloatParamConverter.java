package org.yamikaze.unittest.junit4.parameterized.converter;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 18:09
 */
public class FloatParamConverter implements ParamConverter {

    private final boolean isPrimitive;

    public FloatParamConverter(boolean isPrimitive) {
        this.isPrimitive = isPrimitive;
    }

    @Override
    public Object convert(String val) throws Throwable {
        if (val == null || val.trim().length() == 0) {

            if (isPrimitive) {
                return 0.0F;
            }
            return null;
        }

        return Float.valueOf(val.trim());
    }
}
