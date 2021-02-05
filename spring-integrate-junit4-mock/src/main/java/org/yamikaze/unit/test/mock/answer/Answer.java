package org.yamikaze.unit.test.mock.answer;

import org.yamikaze.unit.test.mock.proxy.InvocationMethod;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-10-31 17:54
 */
public interface Answer {

    Answer INSTANCE = new DefaultValueAnswer();

    /**
     * 返回结果
     * @param invocation 方法调用对象
     * @return           方法返回值
     */
    Object answer(InvocationMethod invocation);

    /**
     * 是否有访问
     * @return true
     */
    boolean accessed();
}
