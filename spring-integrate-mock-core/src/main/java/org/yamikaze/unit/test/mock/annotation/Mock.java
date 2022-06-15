package org.yamikaze.unit.test.mock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mock元数据的处理
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 14:38
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Mocks.class)
public @interface Mock {

    /**
     * 需要mock的类
     * @return class文件
     */
    Class<?> clz();

    /**
     * mock的方法值，支持pattern匹配
     * * 代表所有方法
     * get* 代表所有的get方法
     * @return mock的方法pattern
     */
    String method() default "*";

    /**
     * 是否直接mock异常
     * @return mock异常
     */
    boolean mockException() default false;

    /**
     * 需要mock的异常code
     * @return 异常code
     */
    String exceptionCode() default "normal";

    /**
     * 是否进行返回值模拟
     * @return 默认进行返回值模拟
     */
    boolean mockData() default true;

    /**
     * 默认的返回值key
     * @return object
     */
    String dataKey() default "object";
}
