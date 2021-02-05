package org.yamikaze.unit.test.junit.parameterized.converter;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 18:09
 */
public class BooleanParamConverter implements ParamConverter {

    private boolean isPrimitive;

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

        if ("true".equals(val.trim())) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}
