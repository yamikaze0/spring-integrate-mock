package org.yamikaze.unit.test.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractContextLoader;
import org.springframework.test.context.support.AbstractGenericContextLoader;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-13 01:31
 * cs:off
 */
public class AsyncContextLoader extends AbstractContextLoader {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractGenericContextLoader.class);

    @Override
    public final ConfigurableApplicationContext loadContext(MergedContextConfiguration mergedConfig) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Loading ApplicationContext for merged context configuration [%s].",
                    mergedConfig));
        }

        validateMergedContextConfiguration(mergedConfig);

        GenericApplicationContext context = new GenericApplicationContext(new AsyncApplicationFactory());

        ApplicationContext parent = mergedConfig.getParentApplicationContext();
        if (parent != null) {
            context.setParent(parent);
        }
        prepareContext(context, mergedConfig);
        loadBeanDefinitions(context, mergedConfig);
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
        context.refresh();
        context.registerShutdownHook();

        return context;
    }

    @Override
    public final ConfigurableApplicationContext loadContext(String... locations) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Loading ApplicationContext for locations [%s].",
                    StringUtils.arrayToCommaDelimitedString(locations)));
        }
        GenericApplicationContext context = new GenericApplicationContext(new AsyncApplicationFactory());
        createBeanDefinitionReader(context).loadBeanDefinitions(locations);
        AnnotationConfigUtils.registerAnnotationConfigProcessors(context);
        context.refresh();
        context.registerShutdownHook();
        return context;
    }

    protected void loadBeanDefinitions(GenericApplicationContext context, MergedContextConfiguration mergedConfig) {
        createBeanDefinitionReader(context).loadBeanDefinitions(mergedConfig.getLocations());
    }

    /**
     * Create a new {@link org.springframework.beans.factory.xml.XmlBeanDefinitionReader}.
     * @return a new {@code XmlBeanDefinitionReader}
     */
    protected BeanDefinitionReader createBeanDefinitionReader(GenericApplicationContext context) {
        return new XmlBeanDefinitionReader(context);
    }

    /**
     * Returns {@code "-context.xml"} in order to support detection of a
     * default XML config file.
     */
    @Override
    protected String getResourceSuffix() {
        return "-context.xml";
    }

    protected void validateMergedContextConfiguration(MergedContextConfiguration mergedConfig) {
        if (mergedConfig.hasClasses()) {
            String msg = String.format(
                    "Test class [%s] has been configured with @ContextConfiguration's 'classes' attribute %s, "
                            + "but %s does not support annotated classes.", mergedConfig.getTestClass().getName(),
                    ObjectUtils.nullSafeToString(mergedConfig.getClasses()), getClass().getSimpleName());
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
    }

}
