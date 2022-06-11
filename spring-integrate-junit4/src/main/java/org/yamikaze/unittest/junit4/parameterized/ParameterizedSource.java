package org.yamikaze.unittest.junit4.parameterized;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 11:56
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ParameterizedSource {

    /**
     * The testcase file location.
     * @return file location
     */
    String value();
}
