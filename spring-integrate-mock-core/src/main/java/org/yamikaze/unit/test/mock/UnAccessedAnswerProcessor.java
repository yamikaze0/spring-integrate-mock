package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.answer.Answer;

import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-21 19:14
 */
public interface UnAccessedAnswerProcessor {

    /**
     * 处理单测中未被访问到的Answer
     * @param unAccessedAnswers unused Answers
     */
    void processUnAccessedAnswer(List<Answer> unAccessedAnswers);
}
