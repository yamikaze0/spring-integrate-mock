package org.yamikaze.unit.test.mock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 14:51
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MockCollection {

    /**
     * mock的集合配置
     * @return mock集合配置
     */
    Mock[] mocks();
}
