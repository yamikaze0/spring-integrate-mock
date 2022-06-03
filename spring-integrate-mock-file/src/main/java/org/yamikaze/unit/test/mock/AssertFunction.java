package org.yamikaze.unit.test.mock;

/**
 * @author 三刀
 * @version V1.0 , 2019/10/31
 */
public interface AssertFunction<T> {
    /**
     * 断言结果
     *
     * @param t
     */
    void assertResult(T t);
}
