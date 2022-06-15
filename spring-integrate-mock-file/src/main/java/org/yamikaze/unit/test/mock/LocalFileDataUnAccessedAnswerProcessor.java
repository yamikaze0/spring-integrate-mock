package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.MockRecordHandler.OrderedAnswer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamikaze.unit.test.mock.answer.Answer;

import java.io.File;
import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-22 19:16
 */
public class LocalFileDataUnAccessedAnswerProcessor implements UnAccessedAnswerProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileDataUnAccessedAnswerProcessor.class);

    @Override
    public void processUnAccessedAnswer(List<Answer> unAccessedAnswers) {
        boolean cleanFile = Mockit.MOCKIT.getCleanUnusedMockFile();

        if (!cleanFile) {
            return;
        }

        for (Answer answer : unAccessedAnswers) {
            if (answer instanceof OrderedAnswer) {
                LocalFileDataAnswer localFileDataAnswer = ((OrderedAnswer) answer).getAnswer();
                File file = localFileDataAnswer.getFile();
                boolean delete = file.delete();
                if (delete) {
                    LOGGER.info("delete unused localFileDataAnswer {} success", localFileDataAnswer);
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LocalFileDataUnAccessedAnswerProcessor;
    }
}
