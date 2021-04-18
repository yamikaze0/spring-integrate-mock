package org.yamikaze.unit.test.mock;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.util.AntPathMatcher;

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

    /**
     * The will proxied bean name patterns.
     */
    private final List<String> proxyBeanNamePatterns;

    /**
     * The will proxied classname patterns.
     */
    private final List<String> proxyClassnamePatterns;

    /**
     * The collections classnames that must use jdk proxy.
     */
    private final List<String> mustJdkProxyClassnames;

    private final List<String> jdkProxyBeans = new ArrayList<>(128);

    private final List<String> proxyBeans = new ArrayList<>(256);

    public ProxyBeanMatcher(List<String> ignoreClassPatterns, BeanDefinitionRegistry registry,
                            List<String> proxyBeanNamePatterns, List<String> proxyClassnamePatterns,
                            List<String> mustJdkProxyClassnames) {
        // 忽略spring框架自身的类
        this.ignoreClassPatterns.add("org.springframework.*");
        this.ignoreClassPatterns.addAll(ignoreClassPatterns);
        this.registry = registry;
        this.proxyBeanNamePatterns = proxyBeanNamePatterns;
        this.proxyClassnamePatterns = proxyClassnamePatterns;
        this.mustJdkProxyClassnames = mustJdkProxyClassnames;
    }

    /**
     * Check is exist proxy beans. static check.
     */
    public boolean hasProxyBeans() {
        String[] beanDefinitionNames = registry.getBeanDefinitionNames();
        boolean hasProxyConfig = (!proxyBeanNamePatterns.isEmpty() || !proxyClassnamePatterns.isEmpty());
        return beanDefinitionNames != null && beanDefinitionNames.length != 0 && hasProxyConfig;
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
            String beanClassName = beanDefinition.getBeanClassName();
            String factoryClass = beanDefinition.getFactoryBeanName();

            // 没有被匹配
            if (!matchPattern(proxyBeanNamePatterns, beanName) &&
                    !matchPattern(proxyClassnamePatterns, beanClassName)) {
                continue;
            }


            // 需要忽略代理的bean
            if (matchPattern(ignoreClassPatterns, beanClassName)
                    || matchPattern(ignoreClassPatterns, factoryClass) ) {
                proxyBeans.remove(beanName);
                jdkProxyBeans.remove(beanName);
                continue;
            }

            //针对dubbo需要特别处理 对于Mybatis Mapper也这样处理
            if (Objects.equals(beanClassName, GlobalConfig.DUBBO_SERVICE_BEAN)
                    || Objects.equals(beanClassName, GlobalConfig.APACHE_DUBBO_SERVICE_BEAN)
                    || Objects.equals(beanClassName, "org.mybatis.spring.mapper.MapperFactoryBean")) {
                //dubbo不能被其他cglib mock
                proxyBeans.remove(beanName);

                jdkProxyBeans.remove(beanName);
                jdkProxyBeans.add(beanName);
                continue;
            }

            //Must use jdk proxy.
            if (mustJdkProxyClassnames.contains(beanClassName)) {
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
}
