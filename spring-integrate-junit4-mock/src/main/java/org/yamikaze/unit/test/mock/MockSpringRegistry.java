package org.yamikaze.unit.test.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 17:58
 * cs:off
 */
public class MockSpringRegistry implements BeanDefinitionRegistryPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockSpringRegistry.class);

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private AtomicLong sequence;

    private List<String> ignoreBean = new ArrayList<>(16);

    /**
     * Some bean must registered to parent context.
     */
    private static volatile boolean registered = false;

    public MockSpringRegistry() {
        this.sequence = new AtomicLong(new Random().nextInt(100000));
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        removeBeanDefinition(registry);

        String[] beanDefinitionNames = registry.getBeanDefinitionNames();

        boolean hasMockConfig = (!GlobalConfig.mockClassPattern.isEmpty() || !GlobalConfig.mockBeanNamePattern.isEmpty());

        //没有bean定义 或者 没有mock配置，则不进行mock
        if (beanDefinitionNames == null || beanDefinitionNames.length == 0 || !hasMockConfig) {
            return;
        }

        List<String> mockBeanNames = new ArrayList<>(64);

        //beanName 拦截不为空
        if (!GlobalConfig.mockBeanNamePattern.isEmpty()) {
            for(String beanName : beanDefinitionNames) {
                if (match(GlobalConfig.mockBeanNamePattern, beanName)) {
                    mockBeanNames.remove(beanName);
                    mockBeanNames.add(beanName);
                }
            }
        }

        List<String> mockInterfaceBeanNames = new ArrayList<>(64);

        //bean class name 拦截不为空
        if (!GlobalConfig.mockClassPattern.isEmpty()) {
            extractBeanNameByBeanNamePattern(registry, beanDefinitionNames, mockBeanNames, mockInterfaceBeanNames);
        }

        if (mockBeanNames.isEmpty() && mockInterfaceBeanNames.isEmpty()) {
            return;
        }

        RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(MethodMockInterceptor.class);
        registry.registerBeanDefinition("methodMockInterceptor", rootBeanDefinition);

        if (!mockBeanNames.isEmpty()) {
            registerBeanNameProxy(registry, mockBeanNames, GlobalConfig.isCglibProxy());
        }

        if (!mockInterfaceBeanNames.isEmpty()) {
            //对于dubbo代理一直使用jdk动态代理
            registerBeanNameProxy(registry, mockInterfaceBeanNames, false);
        }
    }

    private void removeBeanDefinition(BeanDefinitionRegistry registry) {
        

    }

    private void extractBeanNameByBeanNamePattern(BeanDefinitionRegistry registry, String[] beanDefinitionNames, List<String> mockBeanNames, List<String> mockInterfaceBeanNames) {
        for(String beanName : beanDefinitionNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (match(ignoreBean, beanClassName)) {
                mockInterfaceBeanNames.remove(beanName);
                mockBeanNames.remove(beanName);
                continue;
            }

            if (!match(GlobalConfig.mockClassPattern, beanDefinition.getBeanClassName())) {
                continue;
            }


            //针对dubbo需要特别处理
            if (Objects.equals(beanClassName, GlobalConfig.DUBBO_SERVICE_BEAN)
                    || Objects.equals(beanClassName, GlobalConfig.APACHE_DUBBO_SERVICE_BEAN)) {
                //dubbo不能被其他mock
                mockBeanNames.remove(beanName);

                mockInterfaceBeanNames.remove(beanName);
                mockInterfaceBeanNames.add(beanName);
                continue;
            }

            //对于Mybatis Mapper也这样处理
            if (Objects.equals(beanClassName, "org.mybatis.spring.mapper.MapperFactoryBean")) {
                mockInterfaceBeanNames.remove(beanName);
                mockInterfaceBeanNames.add(beanName);

                //对于Mybatis Mapper不能被其他mock
                mockBeanNames.remove(beanName);
                continue;
            }

            //Must use jdk proxy.
            if (GlobalConfig.mustJdkMockClassPattern.contains(beanClassName)) {
                mockInterfaceBeanNames.remove(beanName);
                mockInterfaceBeanNames.add(beanName);

                mockBeanNames.remove(beanName);
                continue;
            }

            mockBeanNames.remove(beanName);
            mockBeanNames.add(beanName);

        }
    }

    private void registerBeanNameProxy(BeanDefinitionRegistry registry, List<String> mockBeanNames, boolean proxyTargetProxy) {
        //注册aop拦截器的bean定义
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        ManagedList<TypedStringValue> listBeanNames = new ManagedList<>(mockBeanNames.size());
        for (String beanName : mockBeanNames){
            listBeanNames.add(new TypedStringValue(beanName));
        }
        propertyValues.addPropertyValue("beanNames", listBeanNames);
        ManagedList<TypedStringValue> listInterceptorNames = new ManagedList<>(1);
        listInterceptorNames.add(new TypedStringValue("methodMockInterceptor"));
        propertyValues.addPropertyValue("interceptorNames", listInterceptorNames);

        //支持使用cglib代理，否则会默认从beanDefinition的属性上寻找org.springframework.aop.framework.autoproxy.AutoProxyUtils.preserveTargetClass属性
        //这个属性只能通过Spring的扩展打到beanDefinition上去
        //对于有多重代理的，必须使用cglib代理，因为一旦使用cglib代理后，后续再jdk代理，注入就会失败了
        propertyValues.addPropertyValue("proxyTargetClass", proxyTargetProxy);

        BeanDefinition proxyCreatorBeanDefinition = new RootBeanDefinition(MockBeanNameAutoProxyCreator.class, null, propertyValues);
        registry.registerBeanDefinition("com.yt.buy.test.mockInterceptorProxy#" + next(), proxyCreatorBeanDefinition);
    }

    private long next() {
        return sequence.addAndGet(12L);
    }

    private boolean match(List<String> patterns, String checkPattern) {
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

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    public List<String> getIgnoreBean() {
        return ignoreBean;
    }

    public void setIgnoreBean(List<String> ignoreBean) {
        this.ignoreBean = ignoreBean;
    }
}
