package org.yamikaze.unit.test.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamikaze.unit.test.spi.JsonObjectMapperProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-05-02 18:41
 */
public class LocalFileDataWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileDataWriter.class);

    private static final LocalFileDataWriter WRITER = new LocalFileDataWriter();

    private final List<LocalFileDataWriterTask> taskList = new ArrayList<>(16);

    public static LocalFileDataWriter getWriter() {
        return WRITER;
    }

    public void addTask(File dir, String key, int invokeTimes, MockData data) {
        taskList.add(new LocalFileDataWriterTask(dir, key, invokeTimes, data));
    }

    public void write() {
        for (LocalFileDataWriterTask task : taskList) {
            try {
                task.write();
            } catch (Exception e) {
                LOGGER.warn("write file error, {}/{}{}.json", task.parentFile.getAbsolutePath(), task.key, task.invokeTimes);
                LOGGER.warn("reason is ", e);
            }
        }
    }

    public void clearTask() {
        taskList.clear();
    }

    private static File findUnCreated(File dirFile, String key, int invokeTimes) {
        File file = new File(dirFile, key + invokeTimes + ".json");
        if (!file.exists()) {
            return file;
        }

        LOGGER.warn("file {} already exist!", file.getAbsolutePath());
        return findUnCreated(dirFile, key, invokeTimes + 1);
    }

    /**
     * Local file data task, do write object to file.
     */
    public static class LocalFileDataWriterTask {

        /**
         * The parent.
         */
        private final File parentFile;

        /**
         * Invocation key as filename.
         */
        private final String key;

        /**
         * Invocation orders when same key.
         */
        private final int invokeTimes;

        /**
         * Real-invoked parameters and results.
         */
        private final MockData mockData;

        public LocalFileDataWriterTask(File parentFile, String key, int invokeTimes, MockData mockData) {
            this.parentFile = parentFile;
            this.key = key;
            this.invokeTimes = invokeTimes;
            this.mockData = mockData;
        }

        public void write() {
            //避免跟之前的文件冲突
            File file = findUnCreated(parentFile, key, invokeTimes);
            byte[] bytes = JsonObjectMapperProxy.encode(mockData).getBytes();
            FileOutputStream fileOutputStream = null;

            try {
                LOGGER.info("存储Mock数据至:{}", file.getAbsoluteFile());
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bytes);
                fileOutputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(fileOutputStream);
            }
        }
    }
}
