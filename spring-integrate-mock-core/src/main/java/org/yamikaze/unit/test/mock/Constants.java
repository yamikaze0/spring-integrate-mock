package org.yamikaze.unit.test.mock;

import java.security.PrivilegedAction;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-10-15 11:48
 */
public interface Constants {

    /**
     * The default mock file dir.
     */
    String LOCATION = "/mock/";

    /**
     * Apache dubbo and Alibaba dubbo reference bean classname.
     */
    String DUBBO_SERVICE_BEAN = "com.alibaba.dubbo.config.spring.ReferenceBean";
    String APACHE_DUBBO_SERVICE_BEAN = "org.apache.dubbo.config.spring.ReferenceBean";

    /**
     * Mybatis mapper factory bean
     */
    String MAPPER_BEAN = "org.mybatis.spring.mapper.MapperFactoryBean";

    /**
     * Internal class symbol in full classname
     */
    String INTERNAL_CLASS_SYMBOL = "$";

    /**
     * Class separator.
     * such as org.yamikaze.unittest.check.Checker.
     */
    String CLASS_SEPARATOR = ".";

    /**
     * Jvm class separator.
     * such as org/yamikaze/unittest/check/Checker
     */
    String JVM_CLASS_SEPARATOR = "/";

    /**
     * Os file path separator
     */
    String LINE_SEPARATOR = java.security.AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty("line.separator"));

    String JAVA = "java";

    String CONFIG_CLASSNAME = GlobalConfig.class.getName().replace(CLASS_SEPARATOR, JVM_CLASS_SEPARATOR);
    String MOCK_ENABLED_METHOD = "isMockEnabled";
    String INTERNAL_MOCK_CLASSNAME = InternalMockUtils.class.getName().replace(CLASS_SEPARATOR, JVM_CLASS_SEPARATOR);
}
