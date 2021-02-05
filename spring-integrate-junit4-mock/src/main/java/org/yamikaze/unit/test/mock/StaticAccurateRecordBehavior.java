package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.proxy.InvocationMethod;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-20 10:28
 */
public class StaticAccurateRecordBehavior extends AccurateRecordBehavior {

    @Override
    public boolean match(InvocationMethod invocation) {
        //非静态调用不进行mock
        if (!invocation.getStaticInvoke()) {
            return false;
        }

        if (!this.hasAnswer) {
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

    @Override
    public boolean matchWithoutAnswer(InvocationMethod invocation) {
        return super.match(invocation);
    }

}
