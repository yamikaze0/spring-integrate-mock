package org.yamikaze.unit.test.mock;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-17 10:40
 */
public class MockAssertPostpositionProcessor implements PostpositionProcessor {

    @Override
    public void afterRealInvokeProcess(MethodInvokeTime mit, Object result, Object... args) {
        MockRecord mockRecord = Mockit.MOCKIT.getMockRecord();
        Class clazz = mit.getDeclaringClass();
        Method method = mit.getMethod();
        if (mockRecord == null) {
            return;
        }

        String mockKey = MockRecord.getMockKey(clazz, method);
        Map<String, AssertFunction<Object>> recordAssertMap = mockRecord.recordAssertMap;
        if (recordAssertMap.containsKey(mockKey)) {
            recordAssertMap.get(mockKey).assertResult(result);
        } else {
            List<Class<?>> allInterfaces = ClassUtils.getAllSuperClassesAndInterfaces(clazz);
            for (Class i : allInterfaces) {
                String iMockKey = MockRecord.getMockKey(i, method);
                if (recordAssertMap.containsKey(iMockKey)) {
                    recordAssertMap.get(iMockKey).assertResult(result);
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof MockAssertPostpositionProcessor);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
