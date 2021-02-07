package org.yamikaze.unit.test.mock.event;

import org.yamikaze.unit.test.mock.RecordBehavior;
import org.yamikaze.unit.test.mock.RecordBehaviorList;
import org.yamikaze.unit.test.mock.UnAccessedAnswerProcessor;
import org.yamikaze.unit.test.mock.answer.Answer;
import org.yamikaze.unit.test.mock.answer.CodeAnswer;
import org.yamikaze.unit.test.spi.ExtensionFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-22 19:02
 */
public class UnAccessedAnswerEventListener implements EventListener {

    private static final List<UnAccessedAnswerProcessor> UN_ACCESSED_ANSWER_PROCESSORS = new ArrayList<>(8);

    static {
        List<UnAccessedAnswerProcessor> processors = ExtensionFactory.getExtensions(UnAccessedAnswerProcessor.class);
        UN_ACCESSED_ANSWER_PROCESSORS.addAll(processors);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof TestFinishedEvent)) {
            return;
        }

        List<RecordBehavior> allBehaviors = RecordBehaviorList.INSTANCE.getAllBehaviors();
        List<Answer> unAccessedAnswer = new ArrayList<>(16);

        for (RecordBehavior recordBehavior : allBehaviors) {
            List<Answer> answers = recordBehavior.getAnswers();
            for (Answer answer : answers) {
                if (!answer.accessed() && !(answer instanceof CodeAnswer)) {
                    unAccessedAnswer.add(answer);
                }
            }
        }

        if (!unAccessedAnswer.isEmpty()) {
            unAccessedAnswerProcess(unAccessedAnswer);
        }

    }

    private void unAccessedAnswerProcess(List<Answer> unAccessedAnswer) {
        List<UnAccessedAnswerProcessor> uaaps = UN_ACCESSED_ANSWER_PROCESSORS;
        for (UnAccessedAnswerProcessor unAccessedAnswerProcessor : uaaps) {
            unAccessedAnswerProcessor.processUnAccessedAnswer(unAccessedAnswer);
        }
    }

    public static void addUnAccessedAnswerProcessor(UnAccessedAnswerProcessor unAccessedAnswerProcessor) {
        UN_ACCESSED_ANSWER_PROCESSORS.remove(unAccessedAnswerProcessor);
        UN_ACCESSED_ANSWER_PROCESSORS.add(unAccessedAnswerProcessor);
    }
}
