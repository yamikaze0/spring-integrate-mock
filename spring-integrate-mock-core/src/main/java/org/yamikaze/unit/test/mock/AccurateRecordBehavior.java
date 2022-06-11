package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.answer.Answer;
import org.yamikaze.unit.test.mock.proxy.InvocationMethod;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-20 10:28
 */
public class AccurateRecordBehavior extends RecordBehavior {

    /**
     * Has available answer.
     */
    protected boolean hasAnswer;

    /**
     * All mock config answer size.
     */
    protected int answerSize = 0;

    /**
     * Mocked answer size.
     */
    protected int mockAnswerSize = 0;

    public int getMockAnswerSize() {
        return mockAnswerSize;
    }

    public int getAnswerSize() {
        return answerSize;
    }

    @Override
    public void addAnswer(Answer answer) {
        super.addAnswer(answer);
        answerSize++;
        this.hasAnswer = true;
    }

    @Override
    public synchronized Answer getAnswer() {
        int size = this.getAnswers().size();
        if (size == 1) {
            this.hasAnswer = false;
        }

        mockAnswerSize++;

        return super.getAnswer();
    }

    @Override
    public boolean match(InvocationMethod invocation) {
        if (!this.hasAnswer) {
            return false;
        }

        return super.match(invocation);
    }

    public boolean matchWithoutAnswer(InvocationMethod invocation) {
        return super.match(invocation);
    }
}
