package org.yamikaze.unit.test.mock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2019/10/28
 */
public class Mockit {
    public static final Mockit MOCKIT = new Mockit();
    public static Boolean globalMatchParams = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(Mockit.class);
    private List<String> mockList = new ArrayList<>();
    private boolean mock = false;
    private boolean enabled = false;
    private String absDir;
    private boolean cleanUnusedMockFile;
    private Boolean matchParams;


    private MockRecord mockRecord;
    /**
     * 场景code
     */
    private String sceneCode;

    private String className;

    public boolean getCleanUnusedMockFile() {
        return cleanUnusedMockFile;
    }

    public Mockit cleanUnusedMockFile(boolean cleanUnusedMockFile) {
        this.cleanUnusedMockFile = cleanUnusedMockFile;
        return this;
    }

    public static Mockit mock(boolean mock) {
        MOCKIT.mock = mock;
        MOCKIT.enabled = true;
        return MOCKIT;
    }

    static void clear() {
        LOGGER.info("clear mockList:{}", MOCKIT.mockList);
        MOCKIT.mockList.clear();
        MOCKIT.mock = false;
        MOCKIT.enabled = false;
        MOCKIT.className = null;
        MOCKIT.absDir = null;
        MOCKIT.sceneCode = null;
        MOCKIT.mockRecord = null;
        MOCKIT.cleanUnusedMockFile = false;
    }

    /**
     * 自定义Mock结果
     *
     * @param mockRecord
     */
    public Mockit mock(MockRecord mockRecord) {
        this.mockRecord = mockRecord;
        return this;
    }

    public Mockit matchParam(boolean matchParams) {
        this.matchParams = matchParams;
        return this;
    }

    public static Boolean getGlobalMatchParams() {
        return globalMatchParams;
    }

    public static void setGlobalMatchParams(Boolean matchParam) {
        Mockit.globalMatchParams = matchParam;
    }

    public Boolean getMatchParams() {
        return matchParams;
    }

    public String getMockRecordKey(Class clazz) {
        return clazz.getName();
    }


    public MockRecord getMockRecord() {
        return mockRecord;
    }


    public String getAbsDir() {
        return absDir;
    }

    public void setAbsDir(String absDir) {
        this.absDir = absDir;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getInterfaceAndMethods() {
        return mockList;
    }

    public Mockit mock(List<String> mockData) {
        mockList.addAll(mockData);
        return this;
    }

    boolean isMock() {
        return mock;
    }

    public String getMockDataDir() {
        return StringUtils.replace(className, ".", File.separator) + File.separator + sceneCode;
    }

    public String getSceneCode() {
        return sceneCode;
    }

    public Mockit setSceneCode(String sceneCode) {
        this.sceneCode = sceneCode;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }
}

