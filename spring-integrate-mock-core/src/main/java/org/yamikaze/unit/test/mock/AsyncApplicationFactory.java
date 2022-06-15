package org.yamikaze.unit.test.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.AntPathMatcher;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-13 01:25
 */
public class AsyncApplicationFactory extends DefaultListableBeanFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncApplicationFactory.class);

    private static final ThreadPoolExecutor EXECUTOR = ThreadPoolUtils.getFixExecutor(Runtime.getRuntime().availableProcessors(), 10);

    @Override
    protected void invokeInitMethods(String beanName, Object bean, RootBeanDefinition mbd) throws Throwable {
        if (!needAsyncInit(beanName, bean)) {
            super.invokeInitMethods(beanName, bean, mbd);
            return;
        }

        InitializingBean initializingBean = (InitializingBean) bean;

        //async init client
        EXECUTOR.execute(() -> AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
            try {
                LOGGER.info("async init bean {}", beanName);
                initializingBean.afterPropertiesSet();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }));
    }

    private boolean needAsyncInit(String beanName, Object bean) {
        List<String> patterns = GlobalConfig.getAsyncInitBeanPattern();
        if (!GlobalConfig.isAsyncInitEnabled() || patterns == null || patterns.isEmpty()) {
            return false;
        }

        if (!(bean instanceof InitializingBean)) {
            return false;
        }

        for (String pattern : patterns) {
            AntPathMatcher antPathMatcher = new AntPathMatcher();
            if (antPathMatcher.match(pattern, beanName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void destroySingletons() {
        try {
            if (EXECUTOR.getActiveCount() != 0) {
                EXECUTOR.shutdownNow();
            }
        } catch (Exception e) {
            LOGGER.error("stop async executor error", e);
        }
        super.destroySingletons();
    }
}
