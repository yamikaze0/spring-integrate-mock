package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.proxy.InvocationMethod;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-17 11:10
 */
public class MockRecordBehavior extends RecordBehavior {

    @Override
    public boolean match(InvocationMethod invocation) {
        MockRecord mockRecord = Mockit.MOCKIT.getMockRecord();
        Class clazz = invocation.getDeclaringClass();
        Method method = invocation.getMethod();

        if (mockRecord == null) {
            return false;
        }

        Map<String, Object> recordMap = mockRecord.recordMap;
        Map<String, Throwable> recordThrowableMap = mockRecord.recordThrowableMap;
        String mockKey = MockRecord.getMockKey(clazz, method);
        if (recordMap.containsKey(mockKey)) {
            return true;
        }
        if (recordThrowableMap.containsKey(mockKey)) {
            return true;
        }

        for (Class i : clazz.getInterfaces()) {
            String iMockKey = MockRecord.getMockKey(i, method);
            if (recordMap.containsKey(iMockKey)) {
                return true;
            }
            if (recordThrowableMap.containsKey(iMockKey)) {
                return true;
            }
        }

        return false;
    }
}
