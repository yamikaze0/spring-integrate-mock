package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.answer.Answer;
import org.yamikaze.unit.test.mock.answer.AnswerCollector;
import org.yamikaze.unit.test.mock.answer.MockAnswerCollector;
import org.yamikaze.unit.test.mock.answer.NoopAnswer;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-10-31 18:20
 */
public class YamiMock {

    private static final ThreadLocal<Answer> DO_NOTHING_ANSWER = new ThreadLocal<>();

    public static <T> AnswerCollector<T> when(T result) {
        return new AnswerCollector<>();
    }

    public static <T> AnswerCollector<T> when(T result, boolean matchParams) {
        return new AnswerCollector<>(matchParams);
    }

    public static <T> T doNothing(T mockObject) {
        if (!MockFactory.isMock(mockObject)) {
            throw new IllegalStateException("mockObject is not proxy!");
        }

        DO_NOTHING_ANSWER.set(new NoopAnswer());
        return mockObject;
    }

    public static void clear() {
        DO_NOTHING_ANSWER.remove();
    }

    public static Answer getDoNothingAnswer() {
        return DO_NOTHING_ANSWER.get();
    }


    public static MockAnswerCollector mock(Class<?> mockType) {
        return new MockAnswerCollector(mockType);
    }
}
