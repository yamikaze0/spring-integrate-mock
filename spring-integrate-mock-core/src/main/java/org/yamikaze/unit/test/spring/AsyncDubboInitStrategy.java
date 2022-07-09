package org.yamikaze.unit.test.spring;

import org.springframework.beans.factory.FactoryBean;
import org.yamikaze.unit.test.mock.ThreadPoolUtils;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author qinluo
 * @date 2022-07-08 20:50:44
 * @since 1.0.0
 */
public class AsyncDubboInitStrategy implements DubboInitStrategy {

    private final ThreadPoolExecutor executor = ThreadPoolUtils.getFixExecutor(100, 200);

    @Override
    @SuppressWarnings("unchecked")
    public <T> FactoryBean<T> wrap(String name, FactoryBean<T> origin) {
        AsyncInitFactoryBean asyncInitFactoryBean = new AsyncInitFactoryBean(origin);
        executor.execute(() -> {

            try {
                asyncInitFactoryBean.getObject();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            asyncInitFactoryBean.notify0();
        });

        return (FactoryBean<T>)asyncInitFactoryBean;
    }

    @SuppressWarnings("rawtypes")
    public static class AsyncInitFactoryBean implements FactoryBean {

        /**
         * Async init lock.
         */
        private final Object lock = new Object();

        /**
         * The origin factory bean.
         */
        private final FactoryBean<?> factoryBean;
        private volatile boolean startInitialized = false;
        private volatile boolean initialized = false;
        private volatile boolean waited = false;

        /**
         * The real object.
         */
        private Object object;

        AsyncInitFactoryBean(FactoryBean<?> factoryBean) {
            this.factoryBean = factoryBean;
        }

        @Override
        public Object getObject() throws Exception {
            if (initialized) {
                return object;
            }

            if (startInitialized) {
                waited = true;
                synchronized (lock)  {
                    lock.wait();
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
                synchronized (lock)  {
                    lock.notify();
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
}
