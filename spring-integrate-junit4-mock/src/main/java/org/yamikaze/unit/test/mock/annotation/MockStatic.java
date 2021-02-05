package org.yamikaze.unit.test.mock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-05-18 10:35
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MockStatic {

    /**
     * 需要mock的static class array
     * @return class array
     */
    Class[] value();
}
