package org.yamikaze.unit.test.mock;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ReflectionUtils;
import org.yamikaze.unit.test.mock.config.ProxyConfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2021/4/19 12:33 上午
 */
class ProxyBeanMatcher {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * Ignored classname patterns.
     */
    private final List<String> ignoreClassPatterns = new ArrayList<>(128);

    /**
     * The bean definition registry.
     */
    private final BeanDefinitionRegistry registry;

    private final ProxyConfig conf;

    private final List<String> jdkProxyBeans = new ArrayList<>(128);
    private final List<String> proxyBeans = new ArrayList<>(256);

    public ProxyBeanMatcher(List<String> ignoreClassPatterns, BeanDefinitionRegistry registry, ProxyConfig proxyConf) {
        // 忽略spring框架自身的类
        this.ignoreClassPatterns.add("org.springframework.*");
        this.ignoreClassPatterns.add("org.yamikaze.unittest.*");
        this.ignoreClassPatterns.addAll(ignoreClassPatterns);
        this.registry = registry;
        this.conf = proxyConf;
    }

    /**
     * Check is exist proxy beans. static check.
     */
    public boolean hasProxyBeans() {
        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
        return beanDefinitionNames.length != 0 && conf.hasProxyConf();
    }

    public List<String> getJdkProxyBeans() {
        return jdkProxyBeans;
    }

    public List<String> getProxyBeans() {
        return proxyBeans;
    }

    public void match() {
        // all bean definitions(exclude register in dynamic/sub container).
        String[] beanDefinitionNames = registry.getBeanDefinitionNames();

        for(String beanName : beanDefinitionNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            String beanClassName = getResolvedType(beanDefinition);
            String factoryClass = beanDefinition.getFactoryBeanName();

            // 需要忽略代理的bean
            if (matchPattern(ignoreClassPatterns, beanClassName)
                    || matchPattern(ignoreClassPatterns, factoryClass)) {
                continue;
            }

            // bean blacklist intercepted
            if (matchPattern(conf.getBeanBlacklist(), beanName)) {
                continue;
            }

            // bean class blacklist intercepted
            if (matchPattern(conf.getClassnameBlacklist(), beanClassName)) {
                continue;
            }

            // bean/class white intercepted
            if (!matchPattern(conf.getBeanWhitelist(), beanName)
                    && !matchPattern(conf.getClassnameWhitelist(), beanClassName)) {
                continue;
            }

            //Must use jdk proxy.
            if (matchPattern(conf.getForceJdkProxyWhitelist(), beanClassName)) {
                jdkProxyBeans.remove(beanName);
                jdkProxyBeans.add(beanName);

                proxyBeans.remove(beanName);
                continue;
            }

            proxyBeans.remove(beanName);
            proxyBeans.add(beanName);
        }

    }

    private boolean matchPattern(List<String> patterns, String checkPattern) {
        if (checkPattern == null) {
            return false;
        }

        for (String pattern : patterns) {
            if (Objects.equals(pattern, checkPattern) || PATH_MATCHER.match(pattern, checkPattern)) {
                return true;
            }
        }

        return false;
    }

    private String getResolvedType(BeanDefinition definition) {
        String defaultClassname = definition.getBeanClassName();
        if (defaultClassname != null) {
            return defaultClassname;
        }

        Method getResolvableType = ReflectionUtils.findMethod(BeanDefinition.class, "getResolvableType");

        if (getResolvableType != null) {
            getResolvableType.setAccessible(true);
            ResolvableType resolvableType = (ResolvableType)ReflectionUtils.invokeMethod(getResolvableType, definition);
            if (resolvableType == null || resolvableType == ResolvableType.NONE) {
                return null;
            }

            return resolvableType.getType().getTypeName();
        }

        // resolvableType == null getResolvedFactoryMethod
        Method getResolvedFactoryMethod = ReflectionUtils.findMethod(definition.getClass(), "getResolvedFactoryMethod");
        if (getResolvedFactoryMethod == null) {
            return null;
        }

        getResolvedFactoryMethod.setAccessible(true);
        Method m = (Method)ReflectionUtils.invokeMethod(getResolvedFactoryMethod, definition);
        if (m != null) {
            return m.getReturnType().getName();
        }

        Method getFactoryMethodMetadata = ReflectionUtils.findMethod(definition.getClass(), "getFactoryMethodMetadata");
        if (getFactoryMethodMetadata == null) {
            return null;
        }

        getFactoryMethodMetadata.setAccessible(true);
        MethodMetadata mm = (MethodMetadata)ReflectionUtils.invokeMethod(getFactoryMethodMetadata, definition);
        if (mm != null) {
            return mm.getReturnTypeName();
        }

        return null;
    }
}
