package org.yamikaze.unit.test.degrade;

import org.slf4j.event.Level;
import org.yamikaze.unit.test.degrade.handler.DegradeExceptionHandler;
import org.yamikaze.unit.test.degrade.handler.IgnoredExceptionHandler;
import org.yamikaze.unit.test.degrade.handler.LogDegradeExceptionHandler;
import org.yamikaze.unit.test.degrade.handler.ThrowLogDegradeExceptionHandler;
import org.yamikaze.unit.test.degrade.handler.TimeoutExceptionHandler;
import org.yamikaze.unit.test.degrade.specific.BooleanDegradeExecutor;
import org.yamikaze.unit.test.degrade.specific.ByteDegradeExecutor;
import org.yamikaze.unit.test.degrade.specific.CharacterDegradeExecutor;
import org.yamikaze.unit.test.degrade.specific.DoubleDegradeExecutor;
import org.yamikaze.unit.test.degrade.specific.FloatDegradeExecutor;
import org.yamikaze.unit.test.degrade.specific.IntegerDegradeExecutor;
import org.yamikaze.unit.test.degrade.specific.LongDegradeExecutor;
import org.yamikaze.unit.test.degrade.specific.ShortDegradeExecutor;
import org.yamikaze.unit.test.mark.NotRecommended;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * 降级工具类
 * @author qinluo
 * @version 1.0.0
 * @since 2019-05-17 16:48
 */
public class DegradeUtils {

    /**
     * Current degrade invoke has occurred exception.
     */
    private static final ThreadLocal<Boolean> OCCURRED_ERROR = new ThreadLocal<>();

    /**
     * Current degrade invoke occurred exception.
     */
    private static final ThreadLocal<Exception> OCCURRED_EXCEPTION = new ThreadLocal<>();

    /**
     * 日志记录
     */
    public static final DegradeExceptionHandler ERROR_LOG = new LogDegradeExceptionHandler(Level.ERROR);

    public static final DegradeExceptionHandler WARN_LOG = new LogDegradeExceptionHandler(Level.WARN);

    /**
     * 直接抛出异常，使用DegradeException包装
     */
    public static final DegradeExceptionHandler THROW = new ThrowLogDegradeExceptionHandler();

    public static final DegradeExceptionHandler TIMEOUT = new TimeoutExceptionHandler();

    /**
     * 忽略异常，什么都不做
     */
    public static final DegradeExceptionHandler IGNORE = new IgnoredExceptionHandler();

    private static final Map<Class<?>, Object> DEFAULT_VALUE = new HashMap<>();

    private static final Map<Class<?>, Class<?>> DEGRADE_EXECUTOR_TYPE = new HashMap<>();

    static {
        DEFAULT_VALUE.put(boolean.class, false);
        DEFAULT_VALUE.put(byte.class, (byte)0);
        DEFAULT_VALUE.put(short.class, (short)0);
        DEFAULT_VALUE.put(int.class, 0);
        DEFAULT_VALUE.put(long.class, 0L);
        DEFAULT_VALUE.put(float.class, 0.0f);
        DEFAULT_VALUE.put(double.class, 0D);
        DEFAULT_VALUE.put(char.class, '0');
        DEFAULT_VALUE.put(Boolean.class, false);
        DEFAULT_VALUE.put(Byte.class, (byte)0);
        DEFAULT_VALUE.put(Short.class, (short)0);
        DEFAULT_VALUE.put(Integer.class, 0);
        DEFAULT_VALUE.put(Long.class, 0L);
        DEFAULT_VALUE.put(Float.class, 0.0f);
        DEFAULT_VALUE.put(Double.class, 0D);
        DEFAULT_VALUE.put(Character.class, '0');

        DEGRADE_EXECUTOR_TYPE.put(BooleanDegradeExecutor.class, Boolean.class);
        DEGRADE_EXECUTOR_TYPE.put(IntegerDegradeExecutor.class, Integer.class);
        DEGRADE_EXECUTOR_TYPE.put(LongDegradeExecutor.class, Long.class);
        DEGRADE_EXECUTOR_TYPE.put(ByteDegradeExecutor.class, Byte.class);
        DEGRADE_EXECUTOR_TYPE.put(ShortDegradeExecutor.class, Short.class);
        DEGRADE_EXECUTOR_TYPE.put(FloatDegradeExecutor.class, Float.class);
        DEGRADE_EXECUTOR_TYPE.put(DoubleDegradeExecutor.class, Double.class);
        DEGRADE_EXECUTOR_TYPE.put(CharacterDegradeExecutor.class, Character.class);
    }

    public static <T> T degrade(DegradeExecutor<T> degradeExecutor) {
        return degrade(degradeExecutor, ERROR_LOG, null);
    }

    @NotRecommended
    public static <T> T degrade(ExecutorService executorService, DegradeExecutor<T> degradeExecutor, long timeout) {
        return degrade(executorService, degradeExecutor, ERROR_LOG, null, true, timeout);
    }

    public static void degradeVoid(VoidDegradeExecutor degradeExecutor) {
        degradeVoid(null, degradeExecutor, ERROR_LOG, false, 0);
    }

    @NotRecommended
    public static void degradeVoid(ExecutorService executorService, VoidDegradeExecutor degradeExecutor, long timeout) {
        degradeVoid(executorService, degradeExecutor, ERROR_LOG, true, timeout);
    }

    public static void degradeVoid(VoidDegradeExecutor degradeExecutor, DegradeExceptionHandler handler) {
        degradeVoid(null, degradeExecutor, handler, false, 0);
    }

    public static void degradeVoid(ExecutorService executorService, VoidDegradeExecutor degradeExecutor, DegradeExceptionHandler handler, boolean async, long timeout) {
        checkParams(executorService, (degradeExecutor == null), async, timeout);

        DegradeExceptionHandler handler0 = (handler == null ? IGNORE : handler);

        if (async) {
            Future<Integer> future = executorService.submit(() -> {
                executeVoid(degradeExecutor, handler0);
                return 1;
            });
            try {
                future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                future.cancel(true);
                setOccurred();
                setOccurredException(new TimeoutException("timeout : " + timeout + "ms"));
                TIMEOUT.handler(e);
            }
        } else {
            executeVoid(degradeExecutor, handler0);
        }

    }

    public static <T> T degrade(DegradeExecutor<T> degradeExecutor, T defaultValue) {
        return degrade(degradeExecutor, ERROR_LOG, defaultValue);
    }

    public static <T> T degrade(DegradeExecutor<T> degradeExecutor, DegradeExceptionHandler handler) {
        return degrade(degradeExecutor, handler, null);
    }

    public static <T> T degrade(DegradeExecutor<T> degradeExecutor, DegradeExceptionHandler handler, T defaultValue) {
        return degrade(null, degradeExecutor, handler, defaultValue, false, 0);
    }

    public static <T> T degrade(DegradeExecutor<T> degradeExecutor, DegradeExceptionHandler handler, T defaultValue, boolean async, long timeout) {
        return degrade(null, degradeExecutor, handler, defaultValue, async, timeout);
    }

    /**
     * cs:off
     */
    @SuppressWarnings("unchecked")
    private static <T> T degrade(ExecutorService executorService, DegradeExecutor<T> degradeExecutor, DegradeExceptionHandler handler, T defaultValue, boolean async, long timeout) {
        checkParams(executorService, (degradeExecutor == null), async, timeout);

        Class<T> returnValueType = (degradeExecutor instanceof AbstractDegradeExecutor)
                ? ((AbstractDegradeExecutor<T>) degradeExecutor).getType() : getExecutorType(degradeExecutor);

        final DegradeExceptionHandler innerHandler = (handler == null ? IGNORE : handler);

        T value = defaultValue;

        if (async) {
            Future<T> future = executorService.submit(() -> execute(degradeExecutor, innerHandler));
            try {
                clear();

                value = future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                future.cancel(true);
                setOccurred();
                setOccurredException(new TimeoutException("timeout : " + timeout + "ms"));
                TIMEOUT.handler(e);
            }
        } else {
            value = execute(degradeExecutor, innerHandler);
        }

        if (value == null && hasOccurredException()) {
            return defaultValue;
        }

        if (returnValueType == null) {
            //直接传入DegradeExecutor的情况
            return value;
        }

        //avoid npe
        if (value == null) {
            return (T)DEFAULT_VALUE.get(returnValueType);
        }

        return value;
    }

    private static void checkParams(ExecutorService executorService, boolean nonExecutor, boolean async, long timeout) {
        if (async && timeout <= 0) {
            throw new IllegalArgumentException("timeout must not be great 0!");
        }

        if (nonExecutor) {
            throw new IllegalArgumentException("void degrade executor must not be null!");
        }

        if (async && executorService == null) {
            throw new IllegalArgumentException("executorService must not be null!");
        }
    }

    public static boolean hasOccurredException() {
        return OCCURRED_ERROR.get() != null && OCCURRED_ERROR.get();
    }

    public static Exception occurredException() {
        return OCCURRED_EXCEPTION.get();
    }

    private static void clear() {
        OCCURRED_ERROR.remove();
        OCCURRED_EXCEPTION.remove();
    }

    private static void setOccurred() {
        OCCURRED_ERROR.set(Boolean.TRUE);
    }

    private static void setOccurredException(Exception e) {
        OCCURRED_EXCEPTION.set(e);
    }

    private static <T> T execute(DegradeExecutor<T> degradeExecutor, DegradeExceptionHandler handler) {
        clear();

        try {
            return degradeExecutor.execute();
        } catch (Exception e) {
            setOccurred();
            setOccurredException(e);

            handler.handler(e);
        }
        return null;
    }

    private static void executeVoid(VoidDegradeExecutor degradeExecutor, DegradeExceptionHandler handler) {
        clear();

        try {
            degradeExecutor.execute();
        } catch (Exception e) {
            setOccurred();
            setOccurredException(e);
            handler.handler(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getExecutorType(DegradeExecutor<T> executor) {
        return (Class<T>)DEGRADE_EXECUTOR_TYPE.get(executor.getClass());
    }
}
