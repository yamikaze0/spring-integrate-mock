package org.yamikaze.unit.test.mock.answer;

import org.yamikaze.unit.test.mock.proxy.MockInvocation;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-10-31 18:08
 */
public class ReturnValueAnswer extends AbstractAnswer {

    private final Object value;

    public ReturnValueAnswer(Object value) {
        this.value = value;
    }

    @Override
    public Object answer(MockInvocation invocation) {
        accessed = true;
        return value;
    }

    @Override
    public String toString() {
        return "ReturnValueAnswer {" +
                "value=" + value +
                '}';
    }
}
