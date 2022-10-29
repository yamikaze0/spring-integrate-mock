package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.proxy.MockInvocation;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-20 10:28
 */
public class StaticAccurateRecordBehavior extends RecordBehavior {

    @Override
    public boolean match(MockInvocation invocation) {
        //非静态调用不进行mock
        if (!invocation.getStaticInvoke()) {
            return false;
        }

        if (getClz() != invocation.getDeclaringClass()) {
            return false;
        }

        if (!matchMethod(invocation.getMethod())) {
            return false;
        }

        if (!getMatchParams()) {
            return true;
        }

        return matchParams(invocation.getArgs());
    }

}
