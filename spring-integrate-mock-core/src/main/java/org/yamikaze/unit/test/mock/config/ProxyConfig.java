package org.yamikaze.unit.test.mock.config;

import org.yamikaze.unit.test.mock.Constants;
import org.yamikaze.unit.test.mock.MockException;

import java.util.ArrayList;
import java.util.List;

import static org.yamikaze.unit.test.mock.Constants.APACHE_DUBBO_SERVICE_BEAN;
import static org.yamikaze.unit.test.mock.Constants.DUBBO_SERVICE_BEAN;

/**
 * 采用链式调用设计
 *
 * @author qinluo
 * @date 2022-07-07 23:44:42
 * @since 1.0.0
 */
public class ProxyConfig {

    /**
     * bean黑白名单
     */
    private final List<String> beanWhitelist = new ArrayList<>(16);
    private final List<String> beanBlacklist = new ArrayList<>(16);

    /**
     * bean classname黑白名单
     */
    private final List<String> classnameWhitelist = new ArrayList<>(16);
    private final List<String> classnameBlacklist = new ArrayList<>(16);

    /**
     * 强制jdk代理白名单
     */
    private final List<String> forceJdkProxyWhitelist = new ArrayList<>(8);

    public ProxyConfig() {
        this.includeJdkProxy(Constants.APACHE_DUBBO_SERVICE_BEAN);
        this.includeJdkProxy(Constants.DUBBO_SERVICE_BEAN);
        this.includeJdkProxy(Constants.MAPPER_BEAN);
    }

    /**
     * Include will be proxied bean name pattern.
     *
     * @param pattern bean name pattern.
     * @return        config.
     */
    public ProxyConfig includeBean(String pattern) {
        if (pattern == null) {
            throw new MockException("empty bean name pattern");
        }

        beanWhitelist.remove(pattern);
        beanWhitelist.add(pattern);
        return this;
    }

    /**
     * Exclude will be proxied bean name pattern.
     *
     * @param pattern bean name pattern.
     * @return        config.
     */
    public ProxyConfig excludeBean(String pattern) {
        if (pattern == null) {
            throw new MockException("empty bean name pattern");
        }

        beanWhitelist.remove(pattern);
        beanBlacklist.add(pattern);
        return this;
    }

    /**
     * Include will be proxied bean classname pattern.
     *
     * @param pattern bean classname pattern.
     * @return        config.
     */
    public ProxyConfig includeType(String pattern) {
        if (pattern == null) {
            throw new MockException("empty bean classname pattern");
        }

        classnameWhitelist.remove(pattern);
        classnameWhitelist.add(pattern);
        return this;
    }

    /**
     * Include will be proxied bean class.
     *
     * @param type  bean class.
     * @return      config.
     */
    public ProxyConfig includeType(Class<?> type) {
        if (type == null) {
            throw new MockException("null bean class");
        }
        classnameWhitelist.remove(type.getName());
        classnameWhitelist.add(type.getName());
        return this;
    }

    /**
     * Exclude will be proxied bean classname pattern.
     *
     * @param pattern bean classname pattern.
     * @return        config.
     */
    public ProxyConfig excludeType(String pattern) {
        if (pattern == null) {
            throw new MockException("empty bean classname pattern");
        }

        classnameWhitelist.remove(pattern);
        classnameBlacklist.add(pattern);
        return this;
    }

    /**
     * Exclude will be proxied bean class.
     *
     * @param type  bean class.
     * @return      config.
     */
    public ProxyConfig excludeType(Class<?> type) {
        if (type == null) {
            throw new MockException("null bean class");
        }
        classnameWhitelist.remove(type.getName());
        classnameBlacklist.add(type.getName());
        return this;
    }

    public ProxyConfig includeJdkProxy(String pattern) {
        if (pattern == null) {
            throw new MockException("empty bean classname pattern");
        }
        this.includeType(pattern);
        this.forceJdkProxyWhitelist.remove(pattern);
        this.forceJdkProxyWhitelist.add(pattern);
        return this;
    }

    public ProxyConfig includeJdkProxy(Class<?> type) {
        if (type == null) {
            throw new MockException("null bean class");
        }
        this.includeType(type);
        this.forceJdkProxyWhitelist.remove(type.getName());
        this.forceJdkProxyWhitelist.add(type.getName());
        return this;
    }

    public ProxyConfig excludeJdkProxy(Class<?> type) {
        if (type == null) {
            throw new MockException("null bean class");
        }
        this.forceJdkProxyWhitelist.remove(type.getName());
        return this;
    }

    /**
     * Don't mock anything.
     */
    public void disableProxy() {
        this.forceJdkProxyWhitelist.clear();
        this.classnameBlacklist.clear();
        this.classnameWhitelist.clear();
        this.beanBlacklist.clear();
        this.beanWhitelist.clear();
    }

    /**
     * Don't mock dubbo.
     */
    public void disableDubboProxy() {
        this.excludeType(DUBBO_SERVICE_BEAN);
        this.excludeType(APACHE_DUBBO_SERVICE_BEAN);
    }

    /**
     * Returns has proxy config exists.
     */
    public boolean hasProxyConf() {
        return beanWhitelist.size() > 0 && classnameWhitelist.size() > 0;
    }

    public List<String> getBeanWhitelist() {
        return beanWhitelist;
    }

    public List<String> getBeanBlacklist() {
        return beanBlacklist;
    }

    public List<String> getClassnameWhitelist() {
        return classnameWhitelist;
    }

    public List<String> getClassnameBlacklist() {
        return classnameBlacklist;
    }

    public List<String> getForceJdkProxyWhitelist() {
        return forceJdkProxyWhitelist;
    }
}
