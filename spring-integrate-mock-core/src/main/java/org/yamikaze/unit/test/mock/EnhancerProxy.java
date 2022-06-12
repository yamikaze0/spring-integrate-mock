package org.yamikaze.unit.test.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamikaze.unit.test.enhance.MockClassEnhancer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author qinluo
 * @date 2022-06-12 20:20:30
 * @since 1.0.0
 */
public class EnhancerProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnhancerProxy.class);

    public static void enhanceClasses(List<Class<?>> enhanceClasses) throws UnmodifiableClassException {
        if (enhanceClasses == null || enhanceClasses.isEmpty()) {
            return;
        }

        Instrumentation inst = AgentProxy.getInst();
        if (inst == null || !GlobalConfig.getUseAgentProxy()) {
            return;
        }

        ClassFileTransformer transformer = new ClassFileTransformer() {
            private final List<String> ec = enhanceClasses.stream().map(Class::getName).collect(Collectors.toList());

            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                if (className == null || ModifiedClassHolder.exist(className)) {
                    return null;
                }

                String clzName = className;
                //兼容匿名类、内部类
                if (className.contains(Constants.INTERNAL_CLASS_SYMBOL)) {
                    clzName = className.substring(0, className.indexOf(Constants.INTERNAL_CLASS_SYMBOL));
                }

                if (ec.contains(clzName.replace(Constants.JVM_CLASS_SEPARATOR, Constants.CLASS_SEPARATOR))) {
                    LOGGER.info("enhance class {}", classBeingRedefined.getName());
                    byte[] newClassFileBuffer = new MockClassEnhancer(classfileBuffer).enhanceClass();
                    Class<?> prev = null;
                    if (newClassFileBuffer != null) {
                        prev = ModifiedClassHolder.put(className, classBeingRedefined);
                    }

                    // prev != null means classBeingRedefined has enhanced in other thread.
                    if (prev == null) {
                        return newClassFileBuffer;
                    }
                }

                return null;
            }
        };

        try {
            inst.addTransformer(transformer, true);
            Class<?>[] classes = enhanceClasses.toArray(new Class[0]);
            inst.retransformClasses(classes);
        } finally {
            inst.removeTransformer(transformer);
        }


    }
}
