package org.yamikaze.unit.test.junit.parameterized.converter;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 18:09
 */
public class DoubleParamConverter implements ParamConverter {

    private final boolean isPrimitive;

    public DoubleParamConverter(boolean isPrimitive) {
        this.isPrimitive = isPrimitive;
    }

    @Override
    public Object convert(String val) throws Throwable {
        if (val == null || val.trim().length() == 0) {

            if (isPrimitive) {
                return 0.0D;
            }

            return null;
        }

        return Double.valueOf(val.trim());
    }
}
