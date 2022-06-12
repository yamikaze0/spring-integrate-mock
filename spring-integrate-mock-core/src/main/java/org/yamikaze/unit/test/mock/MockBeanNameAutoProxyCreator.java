package org.yamikaze.unit.test.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-10-12 17:32
 */
public class MockBeanNameAutoProxyCreator extends BeanNameAutoProxyCreator {

    private static final long serialVersionUID = 2189569674372801065L;

    private static final Logger LOGGER = LoggerFactory.getLogger(MockBeanNameAutoProxyCreator.class);

    private static final List<Map<String, Class<?>>> PROXY_TYPE_MAP_LIST = new ArrayList<>();

    private final Map<String, Class<?>> proxyClasses = new ConcurrentHashMap<>(36);

    @SuppressWarnings("unchecked")
    public MockBeanNameAutoProxyCreator() {
        Field proxyTypes = ReflectionUtils.findField(this.getClass(), "proxyTypes");
        if (proxyTypes != null) {
            proxyTypes.setAccessible(true);
            Object field = ReflectionUtils.getField(proxyTypes, this);
            Map<String, Class<?>> map = (Map<String, Class<?>>)field;
            PROXY_TYPE_MAP_LIST.add(map);
        }
        PROXY_TYPE_MAP_LIST.add(proxyClasses);
    }

    @Override
    protected Object getCacheKey(Class<?> beanClass, String beanName) {
        boolean factoryBeanClass = FactoryBean.class.isAssignableFrom(beanClass);
        if (factoryBeanClass) {
            return super.getCacheKey(beanClass, beanName);
        }

        return beanName;
    }

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource targetSource) {
        Object[] proxy = super.getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
        if (proxy != DO_NOT_PROXY &&  Modifier.isFinal(beanClass.getModifiers()) && isProxyTargetClass())  {
            LOGGER.warn("can't subclass final class, bean {}, type = {}", beanName, beanClass.getName());
            return DO_NOT_PROXY;
        }


        boolean factoryBeanClass = FactoryBean.class.isAssignableFrom(beanClass);
        if (proxy != DO_NOT_PROXY) {
            if (!factoryBeanClass) {
                proxyClasses.put(beanName, beanClass);
            }
            return proxy;
        }

        return DO_NOT_PROXY;
    }

    public static String findProxyClassBeanName(Class<?> proxyClass) {
        if (proxyClass == null || PROXY_TYPE_MAP_LIST.isEmpty()) {
            return null;
        }

        for (Map<String, Class<?>> proxyTypeMap : PROXY_TYPE_MAP_LIST) {
            Set<Entry<String, Class<?>>> entries = proxyTypeMap.entrySet();
            for (Entry<String, Class<?>> entry : entries) {
                if (entry.getValue() == proxyClass) {
                    return entry.getKey();
                }
            }

        }

        return null;
    }
}
