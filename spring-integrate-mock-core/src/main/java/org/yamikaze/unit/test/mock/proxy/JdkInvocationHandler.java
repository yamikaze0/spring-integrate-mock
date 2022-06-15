package org.yamikaze.unit.test.mock.proxy;

import org.yamikaze.unit.test.mock.RecordBehavior;
import org.yamikaze.unit.test.mock.RecordBehaviorList;
import org.yamikaze.unit.test.mock.MockUtils;
import org.yamikaze.unit.test.mock.answer.Answer;
import org.yamikaze.unit.test.mock.argument.DefaultArgumentMatcher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-10-31 17:49
 */
public class JdkInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InvocationMethod invocation = new InvocationMethod();
        invocation.setArgs(args);
        invocation.setMethod(method);
        invocation.setProxy(proxy);
        invocation.setTargetClass(proxy.getClass().getInterfaces()[0]);

        Answer answer = Answer.INSTANCE;
        RecordBehavior behavior = RecordBehaviorList.INSTANCE.findRecordBehavior(invocation);
        if (behavior != null) {
            answer = behavior.getAnswer();
        }

        RecordBehavior recordBehavior = RecordBehaviorList.INSTANCE.createRecordBehavior();
        recordBehavior.setClz(proxy.getClass().getInterfaces()[0]);
        recordBehavior.setMethod(method);

        //binding argument matcher
        return recordAndAnswer(method, args, invocation, answer, recordBehavior);
    }

    public static Object recordAndAnswer(Method method, Object[] args, InvocationMethod invocation, Answer answer, RecordBehavior recordBehavior) {
        int index = 0;
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                recordBehavior.addArgumentMatcher(new DefaultArgumentMatcher(arg, method, index++));
            }
        }

        if (MockUtils.getNoopAnswerHolder() != null) {
            recordBehavior.addAnswer(MockUtils.getNoopAnswerHolder());
            MockUtils.clear();
        }

        return answer.answer(invocation);
    }
}
