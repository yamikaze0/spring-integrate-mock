package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.event.UnAccessedAnswerEventListener;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-22 19:06
 */
public class UnAccessAnswerProcessorRegister {

    public static void registerUnAccessedAnswerProcessor(UnAccessedAnswerProcessor unAccessedAnswerProcessor) {
        UnAccessedAnswerEventListener.addUnAccessedAnswerProcessor(unAccessedAnswerProcessor);
    }
}
