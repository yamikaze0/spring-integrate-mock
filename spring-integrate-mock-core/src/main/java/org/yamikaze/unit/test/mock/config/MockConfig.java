package org.yamikaze.unit.test.mock.config;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 16:14
 */
public class MockConfig {

    /**
     * mock的class
     */
    private Class<?> mockClass;

    /**
     * mock的方法pattern
     */
    private String mockMethodPattern;

    /**
     * 是否mock数据
     */
    private boolean mockData;

    /**
     * dataKey
     */
    private String dataKey;

    public Class<?> getMockClass() {
        return mockClass;
    }

    public void setMockClass(Class<?> mockClass) {
        this.mockClass = mockClass;
    }

    public String getMockMethodPattern() {
        return mockMethodPattern;
    }

    public void setMockMethodPattern(String mockMethodPattern) {
        this.mockMethodPattern = mockMethodPattern;
    }

    public boolean isMockData() {
        return mockData;
    }

    public void setMockData(boolean mockData) {
        this.mockData = mockData;
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }
}
