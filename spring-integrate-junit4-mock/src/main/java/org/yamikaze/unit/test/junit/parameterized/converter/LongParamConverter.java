package org.yamikaze.unit.test.junit.parameterized.converter;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 18:09
 */
public class LongParamConverter implements ParamConverter {

    private boolean isPrimitive;

    public LongParamConverter(boolean isPrimitive) {
        this.isPrimitive = isPrimitive;
    }

    @Override
    public Object convert(String val) throws Throwable {
        if (val == null || val.trim().length() == 0) {

            if (isPrimitive) {
                return 0L;
            }

            return null;
        }

        return Long.valueOf(val.trim());
    }
}
