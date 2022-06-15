package org.yamikaze.unit.test.spring;

import org.yamikaze.unit.test.method.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;
import org.yamikaze.unit.test.mock.Constants;
import org.yamikaze.unit.test.mock.ThreadPoolUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-15 16:07
 */
@Order
public class AsyncInitBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncInitBeanPostProcessor.class);
    private final ThreadPoolExecutor executor = ThreadPoolUtils.getFixExecutor(100, 200);
    private final Class<?> referenceClass = getReferenceBeanClass();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean != null && referenceClass != null && referenceClass.isAssignableFrom(bean.getClass())) {
            LOGGER.info("async init bean {}", beanName);
            FactoryBean factoryBean = (FactoryBean)bean;
            Class realType = factoryBean.getObjectType();

            LazyDubboWrapper handler = new LazyDubboWrapper(beanName, factoryBean);
            Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{realType}, handler);

            LazyFactoryBean lazyFactoryBean = new LazyFactoryBean((FactoryBean) bean);
            executor.execute(() -> {

                try {
                    lazyFactoryBean.getObject();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                lazyFactoryBean.notify0();
            });

            return lazyFactoryBean;
            //return new LazyFactoryBeanV2(factoryBean, proxy);
        }

        return bean;
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

    public static class LazyFactoryBeanV2 implements FactoryBean {

        private FactoryBean factoryBean;
        private Object proxy;

        public LazyFactoryBeanV2(FactoryBean factoryBean, Object proxy) {
            this.factoryBean = factoryBean;
            this.proxy = proxy;
        }

        @Override
        public Object getObject() throws Exception {
            return proxy;
        }

        @Override
        public Class<?> getObjectType() {
            return factoryBean.getObjectType();
        }

        @Override
        public boolean isSingleton() {
            return factoryBean.isSingleton();
        }
    }


    public static class LazyFactoryBean implements FactoryBean {

        private final Object obj = new Object();
        private FactoryBean factoryBean;
        private volatile boolean startInitialized = false;
        private volatile boolean initialized = false;
        private volatile boolean waited = false;
        private Object object;

        LazyFactoryBean(FactoryBean factoryBean) {
            this.factoryBean = factoryBean;
        }

        @Override
        public Object getObject() throws Exception {
            if (initialized) {
                return object;
            }

            if (startInitialized) {
                waited = true;
                synchronized (obj)  {
                    obj.wait();
                }
            }

            startInitialized = true;
            object = factoryBean.getObject();
            initialized = true;

            return object;
        }

        void notify0() {
            if (waited) {
                waited = false;
                synchronized (obj)  {
                    obj.notify();
                }
            }

        }

        @Override
        public Class<?> getObjectType() {
            return factoryBean.getObjectType();
        }

        @Override
        public boolean isSingleton() {
            return factoryBean.isSingleton();
        }
    }

    public static class LazyDubboWrapper implements InvocationHandler {

        private FactoryBean referenceBean;

        private String beanName;

        private Object realObj;

        private volatile boolean initialized;

        public LazyDubboWrapper(String beanName, FactoryBean referenceBean) {
            this.beanName = beanName;
            this.referenceBean = referenceBean;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (MethodUtils.isBaseMethod(method)) {
                return mockBaseMethod(method);
            }

            LOGGER.info("invoke wrapper {}-{}", method.getDeclaringClass().getName(), method.getName());

            if (!initialized) {
                init();
            }

            return method.invoke(realObj, args);
        }

        @SuppressWarnings("all")
        private Object mockBaseMethod(Method method) {
            String name = method.getName();
            if (Objects.equals(name, "toString")) {
                return "Wrapper";
            } else if (Objects.equals(name, "hashCode")) {
                return this.hashCode();
            } else if (Objects.equals(name, "equals")) {
                return false;
            } else if (Objects.equals(name, "clone")) {
                throw new UnsupportedOperationException("UnsupportedOperation : Clone");
            } else {
                throw new IllegalStateException("Unknown Base Method : " + name);
            }

        }

        private synchronized void init() {
            if (initialized) {
                return;
            }

            try {
                realObj = referenceBean.getObject();
            } catch (Exception e) {
                throw new RuntimeException("Initialized Bean Error", e);
            }

            initialized = true;
        }
    }
}
