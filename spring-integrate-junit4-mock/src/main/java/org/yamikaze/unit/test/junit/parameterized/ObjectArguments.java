package org.yamikaze.unit.test.junit.parameterized;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 18:37
 */
public class ObjectArguments extends Arguments {

    private Object[] actualArgs;

    public ObjectArguments(Object[] actualArgs) {
        this.actualArgs = actualArgs;
    }

    public Object[] getActualArgs() {
        return actualArgs;
    }

    public void setActualArgs(Object[] actualArgs) {
        this.actualArgs = actualArgs;
    }
}
