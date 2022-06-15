package org.yamikaze.unit.test.mock.answer;

import org.yamikaze.unit.test.mock.DataCodeFactory;
import org.yamikaze.unit.test.mock.proxy.InvocationMethod;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-16 12:08
 */
public class CodeAnswer extends AbstractAnswer {

    /**
     * Answer code.
     */
    private final String code;

    public CodeAnswer(String code) {
        this.code = code;
    }

    @Override
    public Object answer(InvocationMethod invocation) {
        this.accessed = true;
        return DataCodeFactory.getData(code);
    }

    @Override
    public String toString() {
        return "CodeAnswer : {" +
                ", code='" + code + '\'' +
                '}';
    }
}
