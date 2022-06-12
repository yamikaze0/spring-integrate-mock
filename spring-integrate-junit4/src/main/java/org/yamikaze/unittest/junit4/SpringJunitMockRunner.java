package org.yamikaze.unittest.junit4;

import org.junit.rules.TestRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yamikaze.unit.test.enhance.ClassEnhanceDispatcher;
import org.yamikaze.unit.test.enhance.ModifyConfig;
import org.yamikaze.unit.test.mock.AgentProxy;
import org.yamikaze.unit.test.mock.ClassUtils;
import org.yamikaze.unit.test.mock.Constants;
import org.yamikaze.unit.test.mock.EnhancerProxy;
import org.yamikaze.unit.test.mock.GlobalConfig;
import org.yamikaze.unit.test.mock.ModifiedClassHolder;
import org.yamikaze.unit.test.mock.annotation.MockEnhance;
import org.yamikaze.unit.test.mock.MockRunnerHelper;

import java.lang.annotation.Annotation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-05-14 20:57
 * cs:off
 */
public class SpringJunitMockRunner extends SpringJUnit4ClassRunner {

    static {
        if (GlobalConfig.getUseAgentProxy()) {
            AgentProxy.initAgent();
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringJunitMockRunner.class);

    static final MockClassLoader LOADER = new MockClassLoader(Thread.currentThread().getContextClassLoader());

    private static final ClassEnhanceDispatcher DISPATCHER = new ClassEnhanceDispatcher();

    public SpringJunitMockRunner(Class<?> clazz) throws InitializationError {
        super(LOADER.replaceClass(clazz));
    }

    static class MockClassLoader extends ClassLoader  {

        private static final Set<String> MODIFIED_CLASSES = new HashSet<>(16);

        private static final Map<String, ModifyConfig> MODIFIED_MAP = new HashMap<>(16);

        private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        public MockClassLoader(ClassLoader parent) {
            super(parent);
        }

        Class<?> replaceClass(Class<?> clz) {
            MockEnhance mockEnhance = clz.getAnnotation(MockEnhance.class);

            List<Class<?>> allSuperClass = ClassUtils.getAllSuperClass(clz);
            List<Class<?>> enhanceClasses = MockRunnerHelper.extraEnhanceClasses(mockEnhance);

            try {
                enhanceClasses(enhanceClasses);

                for (Class<?> superClz : allSuperClass) {
                    MODIFIED_CLASSES.add(superClz.getName());
                    MODIFIED_MAP.put(superClz.getName(), new ModifyConfig(superClz.getName(), false));
                }

                MODIFIED_CLASSES.add(clz.getName());
                MODIFIED_MAP.put(clz.getName(), new ModifyConfig(clz.getName(), false));

            } catch (Exception e) {
                // ignore
                LOGGER.error("occurred error ", e);
            }

            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

            try {
                Thread.currentThread().setContextClassLoader(LOADER);
                String internalName = clz.getName().replace(".", "/");
                Class<?> modifiedClass = ModifiedClassHolder.get(internalName);
                if (modifiedClass != null) {
                    return modifiedClass;
                }
                return find(clz, clz.getName());
            } catch (Exception e) {
                LOGGER.error("replace class {} fail. can't mock", clz.getName());
                Thread.currentThread().setContextClassLoader(contextClassLoader);
                return clz;
            }

        }

        private void enhanceClasses(List<Class<?>> enhanceClasses) throws UnmodifiableClassException {
            if (enhanceClasses == null || enhanceClasses.isEmpty()) {
                return;
            }

            if (AgentProxy.getInst() == null || !GlobalConfig.getUseAgentProxy()) {
                addAll(enhanceClasses);
            } else {
                EnhancerProxy.enhanceClasses(enhanceClasses);
            }
        }

        private void addAll(List<Class<?>> values) {
            for (Class<?> clz : values) {
                if (clz != null) {
                    MockClassLoader.MODIFIED_CLASSES.add(clz.getName());
                    MODIFIED_MAP.put(clz.getName(), new ModifyConfig(clz.getName(), true));
                }
            }
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class<?> clz = findClass(name);
            if (resolve) {
                resolveClass(clz);
            }

            return clz;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            String internalName = name.replace(".", "/");
            Class<?> modifiedClass = ModifiedClassHolder.get(internalName);
            if (modifiedClass != null) {
                return modifiedClass;
            }

            Class<?> targetClass = classLoader.loadClass(name);
            if (isSystemClass(name) || !shouldModify(name)) {
                return targetClass;
            }

            return find(targetClass, name);

        }

        private boolean shouldModify(String name) {
            //非匿名内部类
            if (!name.contains(Constants.INTERNAL_CLASS_SYMBOL)) {
                return MODIFIED_CLASSES.contains(name);
            }

            //如果是匿名内部类，那么对于外部的类，如果使用了Cl1加载，那么也必须使用Cl1来加载匿名内部类，否则就会无法访问
            String parentClassName = name.substring(0, name.indexOf(Constants.INTERNAL_CLASS_SYMBOL));
            return MODIFIED_CLASSES.contains(parentClassName);
        }

        private boolean isSystemClass(String name) {
            return name.startsWith("java.")
                    || name.startsWith("sun.")
                    || (name.startsWith("org.junit") && !name.contains("org.junit.Assert"))
                    || name.startsWith("org.hamcrest")
                    || name.startsWith("jdk.")
                    || name.startsWith("javax.accessibility")
                    || name.startsWith("org.testng")
                    || name.startsWith("junit.")
                    || name.startsWith("org.pitest.")
                    || (name.startsWith("org.spring") && !name.contains("org.springframework.util.Assert"));


        }

        private Class<?> find(Class<?> targetClass, String name) {
            String className = name;
            if (className.contains(Constants.INTERNAL_CLASS_SYMBOL)) {
                className = name.substring(0, name.indexOf(Constants.INTERNAL_CLASS_SYMBOL));
            }

            byte[] enhancedBytes = DISPATCHER.enhance(targetClass, MODIFIED_MAP.get(className));
            if (enhancedBytes == ClassEnhanceDispatcher.ENHANCE_ERR) {
                return targetClass;
            }

            try {
                Class<?> clz = defineClass(name, enhancedBytes, 0, enhancedBytes.length);
                ModifiedClassHolder.put(name.replace(Constants.CLASS_SEPARATOR, Constants.JVM_CLASS_SEPARATOR), clz);
                LOGGER.info("replace class {}", name);
                return clz;
            } catch (ClassFormatError e) {
                LOGGER.error("load class {} occurred io exception. modify fail.", targetClass.getName());
            }

            return targetClass;
        }
    }

    /**
     * Junit4 extension methods.
     */

    @Override
    protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation,
                                                  boolean isStatic, List<Throwable> errors) {
        // Check test methods : must be void and no args.
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);
        JunitParameterizedRunner.validate(methods, isStatic, errors);
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        return JunitParameterizedRunner.createMethodInvoker(method, test);
    }

    /**
     * @param target the test case instance
     * @return a list of TestRules that should be applied when executing this
     *         test
     */
    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> testRules = super.getTestRules(target);
        if (testRules == null || testRules.isEmpty()) {
            return testRules;
        }

        // filter ExtensionRule
        testRules = testRules.stream().filter(p -> !(p instanceof ExtensionRule)).collect(Collectors.toList());
        return testRules;
    }

    @Override
    protected Statement methodBlock(FrameworkMethod frameworkMethod) {
        Statement statement = super.methodBlock(frameworkMethod);
        return ExtensionRule.getInstance().apply(statement, describeChild(frameworkMethod));
    }
}
