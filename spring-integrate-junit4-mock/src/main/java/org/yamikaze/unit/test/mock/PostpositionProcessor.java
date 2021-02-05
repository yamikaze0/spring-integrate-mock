package org.yamikaze.unit.test.mock;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-17 09:49
 */
public interface PostpositionProcessor {

    /**
     * 真实调用后 后置处理
     * @param mit    调用信息
     * @param result 结果
     * @param args   current invoke params
     */
    void afterRealInvokeProcess(MethodInvokeTime mit, Object result, Object...args);
}
