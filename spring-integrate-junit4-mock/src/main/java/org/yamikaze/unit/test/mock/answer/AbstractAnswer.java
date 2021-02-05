package org.yamikaze.unit.test.mock.answer;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-17 18:17
 */
public abstract class AbstractAnswer implements Answer {

    protected boolean accessed;

    public void setAccessed(boolean accessed) {
        this.accessed = accessed;
    }

    @Override
    public boolean accessed() {
        return accessed;
    }
}
