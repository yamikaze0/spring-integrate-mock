package org.yamikaze.unit.test.mock.proxy;

import org.yamikaze.unit.test.mock.RecordBehavior;
import org.yamikaze.unit.test.mock.RecordBehaviorList;
import org.yamikaze.unit.test.mock.answer.Answer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

import static org.yamikaze.unit.test.mock.proxy.JdkInvocationHandler.recordAndAnswer;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-11 20:00
 */
public class CglibMethodInterceptor implements MethodInterceptor {

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) {
        MockInvocation invocation = new MockInvocation();
        invocation.setArgs(args);
        invocation.setMethod(method);
        invocation.setProxy(o);
        invocation.setTargetClass(o.getClass().getSuperclass());

        Answer answer = Answer.INSTANCE;
        RecordBehavior behavior = RecordBehaviorList.INSTANCE.findRecordBehavior(invocation);
        if (behavior != null) {
            answer = behavior.getAnswer();
        }

        RecordBehavior recordBehavior = RecordBehaviorList.INSTANCE.createRecordBehavior();
        recordBehavior.setMethod(method);
        recordBehavior.setClz(o.getClass().getSuperclass());

        //binding argument matcher..
        return recordAndAnswer(method, args, invocation, answer, recordBehavior);
    }
}
