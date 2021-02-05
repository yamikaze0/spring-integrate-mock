package org.yamikaze.unit.test.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 10:30
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Order {

    /**
     * Extensions order.
     *
     * sort by order value asc.
     *
     * @return order.
     */
    int value() default Integer.MIN_VALUE;
}
