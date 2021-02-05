package org.yamikaze.unit.test.mock.answer;

import org.yamikaze.unit.test.mock.proxy.InvocationMethod;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-12 09:53
 */
public class NoopAnswer extends AbstractAnswer {

    @Override
    public Object answer(InvocationMethod invocation) {
        accessed = true;
        return null;
    }

    @Override
    public String toString() {
        return "NoopAnswer { } ";
    }
}
