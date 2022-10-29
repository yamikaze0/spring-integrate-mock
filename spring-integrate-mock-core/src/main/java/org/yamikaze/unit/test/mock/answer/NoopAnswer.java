package org.yamikaze.unit.test.mock.answer;

import org.yamikaze.unit.test.mock.proxy.MockInvocation;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-12 09:53
 */
public class NoopAnswer extends AbstractAnswer {

    @Override
    public Object answer(MockInvocation invocation) {
        accessed = true;
        return null;
    }

    @Override
    public String toString() {
        return "NoopAnswer { } ";
    }
}
