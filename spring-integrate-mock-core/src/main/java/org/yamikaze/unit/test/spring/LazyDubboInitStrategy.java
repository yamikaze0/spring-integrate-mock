package org.yamikaze.unit.test.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.yamikaze.unit.test.method.MethodUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * @author qinluo
 * @date 2022-07-08 20:50:44
 * @since 1.0.0
 */
public class LazyDubboInitStrategy implements DubboInitStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(LazyDubboInitStrategy.class);

    @Override
    @SuppressWarnings("unchecked")
    public <T> FactoryBean<T> wrap(String name, FactoryBean<T> origin) {
        Class<?> realType = origin.getObjectType();
        LazyDubboInvocationHandler handler = new LazyDubboInvocationHandler(name, origin);
        Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{realType}, handler);
        return new LazyFactoryBean(origin, proxy);
    }

    @SuppressWarnings("rawtypes")
    public static class LazyFactoryBean implements FactoryBean {

        /**
         * The origin factory bean.
         */
        private final FactoryBean<?> factoryBean;

        /**
         * The jdk proxy object.
         */
        private final Object proxy;

        public LazyFactoryBean(FactoryBean<?> factoryBean, Object proxy) {
            this.factoryBean = factoryBean;
            this.proxy = proxy;
        }

        @Override
        public Object getObject() {
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

    public static class LazyDubboInvocationHandler implements InvocationHandler {

        /**
         * Dubbo 's reference bean.
         */
        private final FactoryBean<?> referenceBean;

        private final String beanName;

        private Object realObj;

        private volatile boolean initialized;

        public LazyDubboInvocationHandler(String beanName, FactoryBean<?> referenceBean) {
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
                throw new RuntimeException("Initialized Bean " + beanName + " Error", e);
            }

            initialized = true;
        }
    }
}
