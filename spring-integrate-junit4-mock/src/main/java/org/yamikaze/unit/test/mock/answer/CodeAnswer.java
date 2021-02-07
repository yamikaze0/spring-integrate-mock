package org.yamikaze.unit.test.mock.answer;

import org.yamikaze.unit.test.mock.DataCodeFactory;
import org.yamikaze.unit.test.mock.ExceptionCodeFactory;
import org.yamikaze.unit.test.mock.proxy.InvocationMethod;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-16 12:08
 */
public class CodeAnswer extends AbstractAnswer {

    /**
     * Answer result is exception.
     */
    private final boolean exception;

    /**
     * Answer code.
     */
    private final String code;

    public CodeAnswer(boolean exception, String code) {
        this.exception = exception;
        this.code = code;
    }

    @Override
    public Object answer(InvocationMethod invocation) {
        this.accessed = true;

        Object result;
        if (exception) {
            result = ExceptionCodeFactory.getExceptionByCode(code);
        } else {
            result = DataCodeFactory.getData(code);
        }

        if (result == null && exception) {
            result = DataCodeFactory.getData(code);
        }

        return result;
    }

    @Override
    public String toString() {
        return "CodeAnswer : {" +
                "exception=" + exception +
                ", code='" + code + '\'' +
                '}';
    }
}
