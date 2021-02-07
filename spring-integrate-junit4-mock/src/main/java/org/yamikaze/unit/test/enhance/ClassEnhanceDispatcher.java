package org.yamikaze.unit.test.enhance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-07-22 15:23
 */
public class ClassEnhanceDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassEnhanceDispatcher.class);

    public static final byte[] ENHANCE_ERR = new byte[0];

    public byte[] enhance(Class<?> clz, ModifyConfig config) {
        ClassEnhancer classEnhancer;

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


        classEnhancer = new MockClassEnhancer(classFileBytes);

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
