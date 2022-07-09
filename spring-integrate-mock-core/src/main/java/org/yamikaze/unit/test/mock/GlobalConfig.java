package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.config.ProxyConfig;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 11:15
 * cs:off
 */
public class GlobalConfig {

    /**
     * Mock全局控制开关
     */
    private static boolean mockEnabled = true;

    /**
     * 异步加载bean开关
     */
    private static boolean asyncInitEnabled = true;
    /**
     * 异步加载的bean配置
     */
    private static List<String> ASYNC_INIT_BEAN_PATTERN = new ArrayList<>(16);

    static final ProxyConfig PROXY_CONF = new ProxyConfig();

    /**
     * 默认使用CGLIB代理
     */
    private static boolean CGLIB_PROXY = true;

    private static String saveResourceLocation = Constants.LOCATION;

    private static boolean enableRealInvokeLog = false;
    private static boolean enableDebugLog = false;

    private static boolean useAgentProxy = true;

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

    /**
     * Returns proxy configs.
     */
    public static ProxyConfig proxy() {
        return PROXY_CONF;
    }

    /**
     * 设置Spring使用何种代理方式
     */
    public static void useCglibProxy(boolean useCglibProxy) {
        CGLIB_PROXY = useCglibProxy;
    }

    public static boolean isCglibProxy() {
        return CGLIB_PROXY;
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

    public static boolean isEnabledDebugLog() {
        return enableDebugLog;
    }

    public static void setEnableDebugLog(boolean enableDebugLog) {
        GlobalConfig.enableDebugLog = enableDebugLog;
    }

    public static boolean isAsyncInitEnabled() {
        return GlobalConfig.asyncInitEnabled;
    }

    public static void setAsyncInitEnabled(boolean asyncInitEnabled) {
        GlobalConfig.asyncInitEnabled = asyncInitEnabled;
    }

    public static void setMockEnabled(boolean enabled) {
        GlobalConfig.mockEnabled = enabled;
    }

    public static boolean isMockEnabled() {
        return GlobalConfig.mockEnabled;
    }
}
