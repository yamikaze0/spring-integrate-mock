package org.yamikaze.unit.test.mock.proxy;

import org.yamikaze.unit.test.mock.RecordBehavior;
import org.yamikaze.unit.test.mock.RecordBehaviorList;
import org.yamikaze.unit.test.mock.YamiMock;
import org.yamikaze.unit.test.mock.answer.Answer;
import org.yamikaze.unit.test.mock.argument.DefaultArgumentMatcher;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-11 20:00
 */
public class CglibMethodInterceptor implements MethodInterceptor {

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        InvocationMethod invocation = new InvocationMethod();
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
        int index = 0;
        if (args != null && args.length > 0) {
            for (Object arg : args) {
                recordBehavior.addArgumentMatcher(new DefaultArgumentMatcher(arg, method, index++));
            }
        }

        if (YamiMock.getDoNothingAnswer() != null) {
            recordBehavior.addAnswer(YamiMock.getDoNothingAnswer());
            YamiMock.clear();
        }


        return answer.answer(invocation);
    }
}
