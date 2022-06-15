package org.yamikaze.unit.test.mock.answer;

import org.yamikaze.unit.test.mock.RecordBehavior;
import org.yamikaze.unit.test.mock.RecordBehaviorList;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-10-31 18:21
 */
public class AnswerCollector<T> {

    private final RecordBehavior behavior;

    public AnswerCollector() {
        this.behavior = RecordBehaviorList.INSTANCE.getCurrentRecordBehavior();
    }

    public AnswerCollector(boolean matchParams) {
        this.behavior = RecordBehaviorList.INSTANCE.getCurrentRecordBehavior();
        this.behavior.setMatchParams(matchParams);
    }

    public AnswerCollector<T> matchBean(String beanName) {
        assert beanName != null;
        this.behavior.setBeanName(beanName);
        this.behavior.setMatchBeanName(true);
        return this;
    }

    private void addAnswer(Answer answer) {
        behavior.addAnswer(answer);
    }

    public void thenReturn(T value) {
        addAnswer(new ReturnValueAnswer(value));
    }

    public void thenReturn(T value, T...values) {
        addAnswer(new ReturnValueAnswer(value));

        if (values == null || values.length == 0) {
            return;
        }

        for (T v : values) {
            addAnswer(new ReturnValueAnswer(v));
        }
    }

    public void throwException(Throwable throwable) {
        addAnswer(new ReturnValueAnswer(throwable));
    }
}
