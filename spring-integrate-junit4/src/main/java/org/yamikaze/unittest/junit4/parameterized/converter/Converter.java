package org.yamikaze.unittest.junit4.parameterized.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 17:48
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Converter {

    /**
     * Config param converter
     * @return param converter class
     */
    Class<? extends ParamConverter> value();
}
