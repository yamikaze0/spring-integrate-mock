package org.yamikaze.unittest.junit4.parameterized;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author huqiang
 * @since 2020-08-16 15:13
 */
public class ExcelArgumentFactory implements SupportedArgumentFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ExcelArgumentFactory.class);
    private static final String XLS_SUFFIX = ".xls";
    private static final String XLSX_SUFFIX = ".xlsx";

    @Override
    public boolean supported(String fileLocation) {
        return fileLocation != null &&
                (fileLocation.endsWith(XLS_SUFFIX) || fileLocation.endsWith(XLSX_SUFFIX));
    }

    @Override
    public List<Arguments> loadArguments(String fileLocation) {
        if (fileLocation == null || "".equals(fileLocation.trim())) {
            throw new IllegalArgumentException("fileLocation can't be null");
        }
        URL resource = ExcelArgumentFactory.class.getClassLoader().getResource(fileLocation);
        if (resource == null) {
            throw new RuntimeException("resource can't be null, " + fileLocation);
        }

        ExcelReaderBuilder read = EasyExcel.read(resource.getFile());
        List<Map<Integer, String>> list = read.ignoreEmptyRow(true).sheet().headRowNumber(0).doReadSync();
        List<Arguments> result = new ArrayList<>(10);
        List<String> headList = new ArrayList<>(20);
        for (int i = 0; i < list.size(); i++) {
            Map<Integer, String> map = list.get(i);

            if (map == null || map.isEmpty()) {
                continue;
            }
            List<Param> line = new ArrayList<>(20);
            for (Map.Entry<Integer, String> entry : map.entrySet()) {

                // 设置第一行为字段名称
                if (i == 0) {
                    if (entry.getValue() == null || "".equals(entry.getValue().trim())) {
                        LOG.info("[  excel argument parse  ] column name is null, columnIndex:{} ", entry.getKey());
                        break;
                    }
                    headList.add(entry.getValue().trim());
                    continue;
                }

                if (headList.size() <= entry.getKey()) {
                    LOG.debug("[ excel argument parse ] 参数超过字段数量，lineNumber:{}, columnIndex: {}, columnValue:{} ", i, entry.getKey(), entry.getValue());
                    continue;
                }

                //封装每一个字段
                Param param = new Param();
                param.setLine(i);
                param.setColumnName(headList.get(entry.getKey()));
                param.setColumn(entry.getKey());
                param.setValue(entry.getValue());
                line.add(param);

                LOG.debug("[ excel argument parse ] key: " + entry.getKey() + " ,value:" + entry.getValue());
            }
            if (!line.isEmpty()) {
                result.add(new Arguments(line));
            }
        }
        return result;
    }
}
