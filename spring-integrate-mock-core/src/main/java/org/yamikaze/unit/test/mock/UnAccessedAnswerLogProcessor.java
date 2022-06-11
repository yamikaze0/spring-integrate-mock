package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.answer.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-21 19:17
 */
public class UnAccessedAnswerLogProcessor implements UnAccessedAnswerProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnAccessedAnswerLogProcessor.class);

    @Override
    public void processUnAccessedAnswer(List<Answer> unAccessedAnswers) {
        LOGGER.warn("current test has some config not used, there are:");
        for (Answer answer : unAccessedAnswers) {
            LOGGER.warn("\t{}", answer.toString());
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UnAccessedAnswerLogProcessor;
    }
}
