package org.yamikaze.unittest.junit4;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamikaze.unittest.junit4.parameterized.Arguments;
import org.yamikaze.unittest.junit4.parameterized.Param;
import org.yamikaze.unittest.junit4.parameterized.ParamParser;
import org.yamikaze.unittest.junit4.parameterized.ParameterDescriptor;
import org.yamikaze.unittest.junit4.parameterized.converter.Converter;
import org.yamikaze.unittest.junit4.parameterized.converter.ConverterFactory;
import org.yamikaze.unittest.junit4.parameterized.converter.ParamConverter;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-17 11:35
 */
public class DefaultParamParser implements ParamParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultParamParser.class);

    private static final DefaultParamParser INSTANCE = new DefaultParamParser();

    private DefaultParamParser() {
    }

    public static DefaultParamParser getInstance() {
        return INSTANCE;
    }

    @Override
    public Object[] parse(Arguments arguments, String method, ParameterDescriptor... contexts) {
        if (contexts == null || contexts.length == 0) {
            LOGGER.warn("params contexts is empty. return new Object[0];");
            return new Object[0];
        }

        Object[] actualArgs = new Object[contexts.length];

        List<Param> params = arguments.getParams();
        if (params == null || params.size() == 0) {
            throw new IllegalStateException("There is no arguments.");
        }


        int index = 0;

        // maybe contexts length is less than params size.
        for (; index < contexts.length && index < params.size(); index++) {

            ParameterDescriptor context = contexts[index];
            ParamConverter converter = getParamConverter(context);
            if (converter == null) {
                String msg = "can't find a converter for param at index " + index
                        + " for type " + context.getParameterType().getName();
                throw new IllegalStateException(msg);
            }

            Param arg = params.get(index);

            try {
                actualArgs[index] = converter.convert(arg.getValue());
            } catch (Throwable e) {
                String msg = "parse param index " + index + " for type " + context.getParameterType().getName()
                        + " occurred error, arg column name is " + arg.getColumnName()
                        + " line is " + arg.getLine() + " and column is " + arg.getColumn()
                        + " val is " + arg.getValue() + "";

                throw new IllegalStateException(msg, e);
            }
        }

        return actualArgs;
    }

    private ParamConverter getParamConverter(ParameterDescriptor context) {
        Class<?> converterClazz = getConverterClz(context);

        // If there are no config converter, use parameter's type as converter
        if (converterClazz == null) {
            converterClazz = context.getParameterType();
        }

        return ConverterFactory.getConverter(converterClazz);
    }

    private Class<?> getConverterClz(ParameterDescriptor context) {
        Annotation[] parameterAnnotations = context.getParameterAnnotations();
        for (Annotation annotation : parameterAnnotations) {
            if (annotation.annotationType() == Converter.class) {
                return ((Converter)annotation).value();
            }
        }

        return null;
    }
}
