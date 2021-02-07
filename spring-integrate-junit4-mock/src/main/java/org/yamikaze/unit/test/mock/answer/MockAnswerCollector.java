package org.yamikaze.unit.test.mock.answer;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-05-15 11:07
 */
public class MockAnswerCollector {

    private final Class<?> mockType;

    public MockAnswerCollector(Class<?> mockType) {
        this.mockType = mockType;
    }

    public MethodAnswerCollector mockMethod(String method) {
        return new MethodAnswerCollector(mockType, method);
    }

}
