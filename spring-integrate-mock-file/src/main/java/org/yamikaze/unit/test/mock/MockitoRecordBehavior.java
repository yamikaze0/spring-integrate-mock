package org.yamikaze.unit.test.mock;


import org.yamikaze.unit.test.mock.proxy.MockInvocation;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-17 15:16
 */
public class MockitoRecordBehavior extends RecordBehavior {

    @Override
    public boolean match(MockInvocation invocation) {
        if (!Mockit.MOCKIT.isMock()) {
            return false;
        }

        return super.match(invocation);
    }
}
