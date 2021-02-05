package org.yamikaze.unit.test.mock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-10-15 10:32
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MockScene {

    /**
     * 场景code
     * @return 默认值无任何意义
     */
    String sceneCode() default "default by mock";

    /**
     * 是否不进行mock
     * @return 默认进行mock
     */
    boolean noMock() default false;

    /**
     * 在已有配置的情况下是否进行刷新
     * @return 默认不
     */
    boolean refresh() default false;
}
