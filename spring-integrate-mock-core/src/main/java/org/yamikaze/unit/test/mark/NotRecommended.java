package org.yamikaze.unit.test.mark;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 不推荐使用注解，使用该注解标记的类、方法等不推荐使用
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-18 14:05
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface NotRecommended {

}
