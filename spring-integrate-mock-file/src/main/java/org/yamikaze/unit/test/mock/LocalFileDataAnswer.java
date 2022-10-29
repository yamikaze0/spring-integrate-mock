package org.yamikaze.unit.test.mock;

import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamikaze.unit.test.mock.answer.AbstractAnswer;
import org.yamikaze.unit.test.mock.proxy.MockInvocation;
import org.yamikaze.unit.test.spi.JsonObjectMapperProxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-17 10:56
 */
public class LocalFileDataAnswer extends AbstractAnswer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileDataAnswer.class);

    private File file;

    private MockData mockData;

    public LocalFileDataAnswer(File file) {
        this.file = file;
        check();
    }

    public File getFile() {
        return file;
    }

    public void setMockData(MockData mockData) {
        this.mockData = mockData;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public LocalFileDataAnswer(String filePath) {
        this(new File(filePath));
    }

    private void check() {
        if (!file.exists()) {
            throw new MockException("file " + file.getAbsolutePath() + " not exist!");
        }
    }

    @Override
    public String toString() {
        return "LocalFileDataAnswer : " +
                "file = " + file.getAbsolutePath();
    }

    @Override
    public Object answer(MockInvocation invocation) {
        if (mockData != null) {
            accessed = true;
            return new OriginMockHolder(mockData.getResult());
        }

        accessed = true;

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            StringBuilder sb = new StringBuilder();
            byte[] cache = new byte[1024];
            int i;
            while ((i = inputStream.read(cache)) != -1) {
                sb.append(new String(cache, 0, i));
            }
            mockData = JsonObjectMapperProxy.decode(sb.toString(), new TypeToken<MockData>() {}.getType());
        } catch (IOException e) {
            throw new MockException("load mock file " + file.getAbsolutePath() + " fail.", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }

        LOGGER.info("接口:{} 加载mock数据", invocation.getDeclaringClass().getSimpleName() + "#" + invocation.getMethod().getName());
        return new OriginMockHolder(mockData.getResult());
    }
}
