package org.yamikaze.unit.test.junit.parameterized.converter;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 18:09
 */
public class ByteParamConverter implements ParamConverter {

    private final boolean isPrimitive;

    public ByteParamConverter(boolean isPrimitive) {
        this.isPrimitive = isPrimitive;
    }

    @Override
    public Object convert(String val) throws Throwable {
        if (val == null || val.trim().length() == 0) {
            if (isPrimitive) {
                return (byte)0;
            }

            return null;
        }

        return Byte.valueOf(val.trim());
    }
}
