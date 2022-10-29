package org.yamikaze.unit.test.enhance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamikaze.unit.test.mock.Constants;
import org.yamikaze.unit.test.mock.GlobalConfig;
import org.yamikaze.unit.test.spi.EntryPoints;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
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
    public static final int BEFORE_ENHANCE = 1;
    public static final int AFTER_ENHANCE = 2;

    /**
     * Enhance class occurred error.
     */
    public static final byte[] ENHANCE_ERR = new byte[0];

    /**
     * Enhance class with agent.
     */
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
                    EntryPoints.execute(BEFORE_ENHANCE, new Object[] {className, classfileBuffer});
                    byte[] newClassFileBuffer = new MockClassEnhancer(classfileBuffer).enhanceClass();
                    Class<?> prev = null;
                    if (newClassFileBuffer != null) {
                        prev = ModifiedClassHolder.put(className, classBeingRedefined);
                    }

                    // prev != null means classBeingRedefined has enhanced in other thread.
                    if (prev == null) {
                        EntryPoints.execute(AFTER_ENHANCE, new Object[] {className, newClassFileBuffer});
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

    /**
     * Enhance class without agent.
     */
    public static byte[] enhance(Class<?> clz, ModifyConfig config) {
        String resourceName = clz.getName().replace(".", "/") + ".class";
        InputStream resource = ClassLoader.getSystemResourceAsStream(resourceName);
        if (resource == null) {
            return ENHANCE_ERR;
        }

        byte[] classFileBytes;
        try {
            classFileBytes = readStream(resource);
        } catch (IOException e) {
            LOGGER.error("read stream error, class = {}, e = {}", resourceName, e);
            return ENHANCE_ERR;
        }

        if (config != null && !config.getNeedModify()) {
            return classFileBytes;
        }

        ClassEnhancer classEnhancer = new MockClassEnhancer(classFileBytes);

        try {
            return classEnhancer.enhanceClass();
        } catch (Exception e) {
            LOGGER.error("enhance class {} error, e = {}", resourceName, e);
        }

        return ENHANCE_ERR;
    }

    private static byte[] readStream(final InputStream inputStream) throws IOException {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] data = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                outputStream.write(data, 0, bytesRead);
            }
            outputStream.flush();
            return outputStream.toByteArray();
        }
    }
}
