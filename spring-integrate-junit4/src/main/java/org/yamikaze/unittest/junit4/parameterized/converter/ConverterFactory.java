package org.yamikaze.unittest.junit4.parameterized.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 17:59
 */
public final class ConverterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterFactory.class);

    private static final Set<Class<?>> BASE_TYPES = new HashSet<>();
    private static final Map<Class<?>, ParamConverter> CONVERTERS = new ConcurrentHashMap<>(32);


    public static ParamConverter getConverter(Class<?> clazz) {
        if (BASE_TYPES.contains(clazz)) {
            return CONVERTERS.get(clazz);
        }

        // It's means config clz is null or config clz is not subclass of ParamConverter
        if (!ParamConverter.class.isAssignableFrom(clazz)) {
            LOGGER.warn("clazz {} is not subclass of ParamConverter", clazz.getName());
            return null;
        }

        ParamConverter paramConverter = CONVERTERS.get(clazz);

        if (paramConverter != null) {
            return paramConverter;
        }

        try {
            paramConverter = (ParamConverter)clazz.newInstance();
            CONVERTERS.put(clazz, paramConverter);

            return paramConverter;
        } catch (Exception e) {
            throw new IllegalStateException("construct clazz " + clazz.getName() + " fail", e);
        }
    }


    private ConverterFactory() {

    }

    static {
        BASE_TYPES.add(byte.class);
        BASE_TYPES.add(short.class);
        BASE_TYPES.add(int.class);
        BASE_TYPES.add(long.class);
        BASE_TYPES.add(float.class);
        BASE_TYPES.add(double.class);
        BASE_TYPES.add(char.class);
        BASE_TYPES.add(boolean.class);
        BASE_TYPES.add(Integer.class);
        BASE_TYPES.add(Byte.class);
        BASE_TYPES.add(Short.class);
        BASE_TYPES.add(Long.class);
        BASE_TYPES.add(Float.class);
        BASE_TYPES.add(Double.class);
        BASE_TYPES.add(Boolean.class);
        BASE_TYPES.add(Character.class);
        BASE_TYPES.add(String.class);
        BASE_TYPES.add(Date.class);


        CONVERTERS.put(byte.class, new ByteParamConverter(true));
        CONVERTERS.put(short.class, new ShortParamConverter(true));
        CONVERTERS.put(int.class, new IntegerParamConverter(true));
        CONVERTERS.put(long.class, new LongParamConverter(true));
        CONVERTERS.put(float.class, new FloatParamConverter(true));
        CONVERTERS.put(double.class, new DoubleParamConverter(true));
        CONVERTERS.put(char.class, new CharParamConverter(true));
        CONVERTERS.put(boolean.class, new BooleanParamConverter(true));
        CONVERTERS.put(Integer.class, new IntegerParamConverter(false));
        CONVERTERS.put(Byte.class, new ByteParamConverter(false));
        CONVERTERS.put(Short.class, new ShortParamConverter(false));
        CONVERTERS.put(Long.class, new LongParamConverter(false));
        CONVERTERS.put(Float.class, new FloatParamConverter(false));
        CONVERTERS.put(Double.class, new DoubleParamConverter(false));
        CONVERTERS.put(Boolean.class, new BooleanParamConverter(false));
        CONVERTERS.put(Character.class, new CharParamConverter(false));
        CONVERTERS.put(String.class, new StringParamConverter());
        CONVERTERS.put(Date.class, new DateParamConverter());

    }
}
