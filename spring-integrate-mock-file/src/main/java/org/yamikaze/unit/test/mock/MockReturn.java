package org.yamikaze.unit.test.mock;

/**
 * @author 三刀
 * @version V1.0 , 2019/10/31
 */
public class MockReturn<T> {
    private MockRecord record;

    public MockReturn(MockRecord tMockRecord) {
        this.record = tMockRecord;
    }

    /**
     * Mock接口结果
     *
     * @param value
     * @return
     */
    public MockRecord thenReturn(T value) {
        record.recordMap.put(record.currentMockMethod, value);
        return record;
    }

    /**
     * Mock接口异常
     *
     * @param throwable
     * @return
     */
    public MockRecord thenThrow(Throwable throwable) {
        record.recordThrowableMap.put(record.currentMockMethod, throwable);
        return record;
    }

    /**
     * 断言接口结果,无法断言异常
     *
     * @param function
     * @return
     */
    public MockRecord thenAssert(AssertFunction<T> function) {
        record.recordAssertMap.put(record.currentMockMethod, (AssertFunction<Object>) function);
        return record;
    }
}
