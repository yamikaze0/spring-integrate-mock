package org.yamikaze.unit.test.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

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

    private static List<Map<String, Class>> proxyTypeMapList = new ArrayList<>();

    private Map<String, Class> proxyClasses = new ConcurrentHashMap<>(36);

    public MockBeanNameAutoProxyCreator() {
        Field proxyTypes = ReflectionUtils.findField(this.getClass(), "proxyTypes");
        if (proxyTypes != null) {
            proxyTypes.setAccessible(true);
            Object field = ReflectionUtils.getField(proxyTypes, this);
            Map<String, Class> map = (Map<String, Class>)field;
            proxyTypeMapList.add(map);
        }
        proxyTypeMapList.add(proxyClasses);

    }

    @Override
    protected Object getCacheKey(Class<?> beanClass, String beanName) {
        boolean factoryBeanClass = FactoryBean.class.isAssignableFrom(beanClass);
        if (factoryBeanClass) {
            return super.getCacheKey(beanClass, beanName);
        }

        return beanName;
    }

    private List<String> beanNames;


    /**
     * Set the names of the beans that should automatically get wrapped with proxies.
     * A name can specify a prefix to match by ending with "*", e.g. "myBean,tx*"
     * will match the bean named "myBean" and all beans whose name start with "tx".
     * <p><b>NOTE:</b> In case of a FactoryBean, only the objects created by the
     * FactoryBean will get proxied. This default behavior applies as of Spring 2.0.
     * If you intend to proxy a FactoryBean instance itself (a rare use case, but
     * Spring 1.2's default behavior), specify the bean name of the FactoryBean
     * including the factory-bean prefix "&": e.g. "&myFactoryBean".
     * @see org.springframework.beans.factory.FactoryBean
     * @see org.springframework.beans.factory.BeanFactory#FACTORY_BEAN_PREFIX
     */
    @Override
    public void setBeanNames(String... beanNames) {
        Assert.notEmpty(beanNames, "'beanNames' must not be empty");
        this.beanNames = new ArrayList<String>(beanNames.length);
        for (String mappedName : beanNames) {
            this.beanNames.add(StringUtils.trimWhitespace(mappedName));
        }
        super.setBeanNames(beanNames);
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

        if (factoryBeanClass && beanNames.contains(beanName)) {
            return DO_NOT_PROXY;
        }

        return DO_NOT_PROXY;
    }

    public static String findProxyClassBeanName(Class proxyClass) {
        if (proxyClass == null || proxyTypeMapList.isEmpty()) {
            return null;
        }

        for (Map<String, Class> proxyTypeMap : proxyTypeMapList) {
            Set<Entry<String, Class>> entries = proxyTypeMap.entrySet();
            for (Entry<String, Class> entry : entries) {
                if (entry.getValue() == proxyClass) {
                    return entry.getKey();
                }
            }

        }

        return null;
    }
}
