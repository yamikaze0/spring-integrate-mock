package org.yamikaze.unit.test.enhance;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-07-22 15:20
 */
public interface ClassEnhancer {

    /**
     * Object classname.
     */
    String JAVA_LANG_OBJECT = Object.class.getName();

    /**
     * The Array descriptor character in jvm.
     */
    String ARRAY_DIMENSION_CHAR = "[";

    /**
     * Enhance class and return class bytes.
     *
     * @return class bytes.
     */
    byte[] enhanceClass();
}
