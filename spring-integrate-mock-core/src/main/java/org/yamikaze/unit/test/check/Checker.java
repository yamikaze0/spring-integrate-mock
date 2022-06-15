package org.yamikaze.unit.test.check;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 14:08
 */
public interface Checker {

    /**
     * 方法必须以test开始
     */
    String METHOD_TEST_PREFIX = "test";

    /**
     * 类名必须以Test开始或者Test结尾
     */
    String CLASS_TEST_SUFFIX = "Test";

    String CLASS_TEST_PREFIX = "Test";


    /**
     * 检查，主要强制规范检查单测命名
     * @param description   Test方法描述
     */
    void check(MethodDescriptor description);
}
