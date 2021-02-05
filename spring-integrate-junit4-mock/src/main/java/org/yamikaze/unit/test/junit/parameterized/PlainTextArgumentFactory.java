package org.yamikaze.unit.test.junit.parameterized;

import org.yamikaze.unit.test.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Only process plain text file.
 * <p>
 * It's content format must as follow:
 * <p>
 * The first line is title, and special title header is comment.
 *
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 17:30
 */
public class PlainTextArgumentFactory implements ArgumentFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PlainTextArgumentFactory.class);
    public static final String CHARSET = "UTF-8";
    static final String SUFFIX = ".txt";

    static final ArgumentFactory INSTANCE = new PlainTextArgumentFactory();

    @Override
    public List<Arguments> loadArguments(String fileLocation) {
        if (fileLocation == null || "".equals(fileLocation.trim())) {
            throw new IllegalArgumentException("fileLocation can't be null");
        }
        URL resource = PlainTextArgumentFactory.class.getClassLoader().getResource(fileLocation);
        if (resource == null) {
            throw new RuntimeException("resource can't be null, " + fileLocation);
        }

        List<Arguments> results = new ArrayList<>(10);
        BufferedReader bufferedReader = null;
        try (InputStream inputStream = resource.openStream()) {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, CHARSET));

            String readLine = bufferedReader.readLine();
            int idx = 1;
            List<String> headList = null;
            while (readLine != null) {
                if ("".equals(readLine.trim())) {
                    LOG.warn("[ plainText parse arguments ] line content is empty, lineNumber: {} ", idx);
                    idx++;
                    readLine = bufferedReader.readLine();
                    continue;
                }

                if (headList == null) {
                    headList = parseHead(readLine);
                } else {
                    List<Param> params = parse(readLine, headList, idx - 1);
                    results.add(new Arguments(params));
                }

                idx++;
                readLine = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(bufferedReader);
        }

        return results;
    }

    private List<String> parseHead(String content) {
        List<String> result = new ArrayList<>(20);
        // 默认按照 | 前面加空格的方式进行分隔
        String[] split = content.split("\\|");
        Collections.addAll(result, split);
        return result;
    }

    private List<Param> parse(String content, List<String> headList, int lineNumber) {
        List<Param> result = new ArrayList<>(20);
        // 默认按照 | 前面加空格的方式进行分隔
        String[] split = content.split(" \\| ");
        for (int i = 0; i < split.length; i++) {
            // 超过字段数量的值将会被丢弃
            if (headList.size() <= i) {
                LOG.warn("[ plainText parse arguments ] 解析参数超过字段数量: {} ", split[i]);
                continue;
            }
            Param param = new Param();
            param.setColumn(i);
            param.setLine(lineNumber);
            param.setValue(split[i]);
            param.setColumnName(headList.get(i));
            result.add(param);
        }
        return result;
    }
}
