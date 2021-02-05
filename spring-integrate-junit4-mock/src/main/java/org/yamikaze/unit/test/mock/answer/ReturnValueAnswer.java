package org.yamikaze.unit.test.mock.answer;

import org.yamikaze.unit.test.mock.proxy.InvocationMethod;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-10-31 18:08
 */
public class ReturnValueAnswer extends AbstractAnswer {

    private Object value;

    public ReturnValueAnswer(Object value) {
        this.value = value;
    }

    @Override
    public Object answer(InvocationMethod invocation) {
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
