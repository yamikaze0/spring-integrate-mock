package org.yamikaze.unit.test.degrade;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-12 14:53
 */
public class AsyncInvokeRejectHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (executor.isShutdown()) {
            return;
        }

        new Thread(() -> r.run()).start();
    }
}
