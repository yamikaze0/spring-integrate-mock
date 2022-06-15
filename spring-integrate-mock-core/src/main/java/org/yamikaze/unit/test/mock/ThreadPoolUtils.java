package org.yamikaze.unit.test.mock;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author qinluo
 * @since 2019-03-07 01:22
 */
public class ThreadPoolUtils {

    public static ThreadPoolExecutor getFixExecutor(int maxSize, int maxQueueSize) {
        return new ThreadPoolExecutor(maxSize, maxSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(maxQueueSize), Thread::new, new ThreadPoolExecutor.AbortPolicy());

    }
}
