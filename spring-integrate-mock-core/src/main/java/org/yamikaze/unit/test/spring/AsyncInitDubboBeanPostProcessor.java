package org.yamikaze.unit.test.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;
import org.yamikaze.unit.test.mock.Constants;
import org.yamikaze.unit.test.mock.GlobalConfig;

import java.util.Objects;

/**
 * Dubbo异步初始化处理器，对于使用注解@Reference引用的服务无法生效
 *
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-15 16:07
 */
@Order
public class AsyncInitDubboBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncInitDubboBeanPostProcessor.class);
    private static final String SELECTED_STRATEGY = "dubbo.init.strategy";
    private static final String ASYNC_INIT = "asyncInit";
    private static final String LAZY_INIT = "lazyInit";
    private static final DubboInitStrategy LAZY_STRATEGY = new LazyDubboInitStrategy();
    private static final DubboInitStrategy ASYNC_STRATEGY = new AsyncDubboInitStrategy();
    private static final DubboInitStrategy NOOP = new NoopDubboInitStrategy();

    private final Class<?> referenceClass = getReferenceBeanClass();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (referenceClass == null) {
            return bean;
        }

        // noinspection ConstantConditions
        if (bean == null || !referenceClass.isAssignableFrom(bean.getClass())) {
            return bean;
        }

        if (GlobalConfig.isEnabledDebugLog()) {
            LOGGER.info("async init bean {}", beanName);
        }

        FactoryBean<?> factoryBean = (FactoryBean<?>)bean;

        String strategy = System.getProperty(SELECTED_STRATEGY, ASYNC_INIT);
        DubboInitStrategy initStrategy;
        if (Objects.equals(strategy, ASYNC_INIT)) {
            initStrategy = ASYNC_STRATEGY;
        } else if (Objects.equals(strategy, LAZY_INIT)) {
            initStrategy = LAZY_STRATEGY;
        } else {
            initStrategy = NOOP;
        }

        return initStrategy.wrap(beanName, factoryBean);
    }

    private static Class<?> getReferenceBeanClass() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Class<?> refClz = null;
        try {
            refClz = ClassUtils.forName(Constants.APACHE_DUBBO_SERVICE_BEAN, contextClassLoader);
            LOGGER.info("Using Apache Dubbo....");
        } catch (ClassNotFoundException e) {
            //No-op
        }

        if (refClz == null) {
            try {
                refClz = ClassUtils.forName(Constants.DUBBO_SERVICE_BEAN, contextClassLoader);
                LOGGER.info("Using Alibaba Dubbo....");
            } catch (ClassNotFoundException e) {
                //No-op
            }
        }

        return refClz;
    }
}
