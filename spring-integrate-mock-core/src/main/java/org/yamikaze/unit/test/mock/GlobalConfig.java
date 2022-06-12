package org.yamikaze.unit.test.mock;

import java.util.ArrayList;
import java.util.List;

import static org.yamikaze.unit.test.mock.Constants.APACHE_DUBBO_SERVICE_BEAN;
import static org.yamikaze.unit.test.mock.Constants.DUBBO_SERVICE_BEAN;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 11:15
 * cs:off
 */
public class GlobalConfig {

    /**
     * 异常开关全局设置
     */
    private static boolean SWITCH = true;

    /**
     * 异步加载bean开关
     */
    private static boolean ASYNC_INIT_SWITCH = true;
    /**
     * 异步加载的bean配置
     */
    private static List<String> ASYNC_INIT_BEAN_PATTERN = new ArrayList<>(16);

    /**
     * Mocked Class name pattern.
     */
    static List<String> mockClassPattern = new ArrayList<>(16);

    /**
     * Mocked bean name pattern.
     */
    static List<String> mockBeanNamePattern = new ArrayList<>(16);

    /**
     * The Collection of full-class that must use jdk proxy, such as DUBBO_SERVICE_BEAN, Mybatis Mapper etc.
     */
    static List<String> mustJdkMockClassPattern = new ArrayList<>(16);

    /**
     * 默认使用CGLIB代理
     */
    private static boolean CGLIB_PROXY = true;

    private static String saveResourceLocation = Constants.LOCATION;

    private static boolean enableUsageLog = false;

    private static boolean enableRealInvokeLog = false;

    private static boolean useAgentProxy = true;

    static {
        mockClassPattern.add(DUBBO_SERVICE_BEAN);
        mockClassPattern.add(APACHE_DUBBO_SERVICE_BEAN);
    }

    public static boolean getUseAgentProxy() {
        return useAgentProxy;
    }

    public static void setUseAgentProxy(boolean useAgentProxy) {
        GlobalConfig.useAgentProxy = useAgentProxy;
    }

    public static boolean getEnableRealInvokeLog() {
        return enableRealInvokeLog;
    }

    public static void setEnableRealInvokeLog(boolean enableRealInvokeLog) {
        GlobalConfig.enableRealInvokeLog = enableRealInvokeLog;
    }

    public static boolean getEnableUsageLog() {
        return enableUsageLog;
    }

    public static void setEnableUsageLog(boolean enableUsageLog) {
        GlobalConfig.enableUsageLog = enableUsageLog;
    }

    public static String getSaveResourceLocation() {
        return saveResourceLocation;
    }

    public static void setSaveResourceLocation(String saveResourceLocation) {
        if (saveResourceLocation == null) {
            throw new IllegalArgumentException("saveResourceLocation must not be null!");
        }

        if (!saveResourceLocation.endsWith("/")) {
            GlobalConfig.saveResourceLocation = saveResourceLocation + "/";
        } else {
            GlobalConfig.saveResourceLocation = saveResourceLocation;
        }
    }

    public static void useCglibProxy() {
        CGLIB_PROXY = true;
    }

    public static void useJdkProxy() {
        CGLIB_PROXY = false;
    }

    public static boolean isCglibProxy() {
        return CGLIB_PROXY;
    }

    public static void addMockPattern(String pattern) {
        if (pattern == null || pattern.trim().length() == 0) {
            throw new IllegalArgumentException("mock pattern must not be blank!");
        }

        mockClassPattern.remove(pattern);
        mockClassPattern.add(pattern);
    }

    public static void addBeanNamePattern(String pattern) {
        if (pattern == null || pattern.trim().length() == 0) {
            throw new IllegalArgumentException("mock pattern must not be blank!");
        }

        mockBeanNamePattern.remove(pattern);
        mockBeanNamePattern.add(pattern);
    }

    public static void removeBeanNamePattern(String pattern) {
        mockBeanNamePattern.remove(pattern);
    }

    public static void noMockBean() {
        mockBeanNamePattern.clear();
    }

    public static boolean isMockDubbo() {
        return mockClassPattern.contains(DUBBO_SERVICE_BEAN)
                || mockClassPattern.contains(APACHE_DUBBO_SERVICE_BEAN);
    }

    public static void noMock() {
        mockClassPattern.clear();
    }

    public static void noMockDubbo() {
        mockClassPattern.remove(DUBBO_SERVICE_BEAN);
        mockClassPattern.remove(APACHE_DUBBO_SERVICE_BEAN);
    }

    public static void addMockClass(Class<?> clz) {
        if (clz == null) {
            throw new IllegalArgumentException("mock class must not be null!");
        }

        mockClassPattern.remove(clz.getName());
        mockClassPattern.add(clz.getName());
    }

    public static void addMustJdkMock(Class<?> clz) {
        if (clz == null) {
            throw new IllegalArgumentException("mock class must not be null!");
        }

        if (!clz.isInterface()) {
            throw new IllegalArgumentException("class must be an interface.");
        }

        mockClassPattern.remove(clz.getName());
        mockClassPattern.add(clz.getName());

        mustJdkMockClassPattern.add(clz.getName());
    }

    public static void removeMustJdkMock(Class<?> clz) {
        if (clz == null) {
            throw new IllegalArgumentException("mock class must not be null!");
        }

        mockClassPattern.remove(clz.getName());
        mustJdkMockClassPattern.remove(clz.getName());
    }

    public static void removeMockClass(Class<?> clz) {
        if (clz == null) {
            throw new IllegalArgumentException("mock class must not be null!");
        }

        mockClassPattern.remove(clz.getName());
    }

    public static List<String> getAsyncInitBeanPattern() {
        return ASYNC_INIT_BEAN_PATTERN;
    }

    public static void setAsyncInitBeanPattern(List<String> asyncInitBeanPattern) {
        ASYNC_INIT_BEAN_PATTERN = asyncInitBeanPattern;
    }

    public static void addAsyncInitBeanPattern(String pattern) {
        if (pattern == null) {
            throw new IllegalStateException("pattern must not be null");
        }
        ASYNC_INIT_BEAN_PATTERN.remove(pattern);
        ASYNC_INIT_BEAN_PATTERN.add(pattern);

    }

    public static boolean isAsyncInitSwitch() {
        return ASYNC_INIT_SWITCH;
    }

    public static void setAsyncInitSwitch(boolean asyncInitSwitch) {
        ASYNC_INIT_SWITCH = asyncInitSwitch;
    }

    public static void resetSwitch(boolean flag) {
        SWITCH = flag;
    }

    public static void resetSwitch() {
        resetSwitch(true);
    }

    public static boolean getSwitch() {
        return SWITCH;
    }
}
