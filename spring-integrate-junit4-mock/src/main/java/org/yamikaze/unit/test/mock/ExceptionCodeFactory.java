package org.yamikaze.unit.test.mock;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 17:13
 */
public class ExceptionCodeFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCodeFactory.class);

    public static final String COMMON_CODE = "normal";

    private static final Map<String, Exception> EXCEPTION_MAP = new HashMap<>(32);

    static {
        EXCEPTION_MAP.put(COMMON_CODE, new MockException("normal code"));
    }

    public static Exception getExceptionByCode(String code) {
        if (code == null) {
            throw new IllegalStateException("exception code must not be null!");
        }

        Exception exception = EXCEPTION_MAP.get(code);
        if (exception == null) {
            LOGGER.warn("there is no exception code mapping for code {}", code);
        }

        return exception;
    }

    public static void register(String code, Exception e) {
        if (code == null) {
            throw new IllegalStateException("exception code must not be null!");
        }

        if (e == null) {
            throw new IllegalStateException("exception must not be null");
        }

        EXCEPTION_MAP.put(code, e);
    }

    public static void registerByJson(String json) {
        JSONObject object = null;
        try {
            object = JSONObject.parseObject(json);
        } catch (Exception e) {
            throw new IllegalStateException("parse json error.", e);
        }

        if (object == null) {
            throw new IllegalStateException("not support empty json for register!");
        }

        try {
            String exceptionCode = object.getString("exceptionCode");
            String exceptionClass = object.getString("exceptionClass");
            String message = object.getString("exceptionMessage");

            if (exceptionCode == null) {
                throw new MockException("exception code is blank");
            }

            if (exceptionClass == null) {
                throw new ClassNotFoundException("exception class is blank");
            }

            Class<?> clz = Class.forName(exceptionClass);
            if (!Exception.class.isAssignableFrom(clz)) {
                throw new MockException("class " + exceptionClass + " is not a exception class");
            }

            Constructor<?> constructor = clz.getConstructor(String.class);
            constructor.setAccessible(true);
            Exception o = (Exception) constructor.newInstance(message);
            Exception put = EXCEPTION_MAP.put(exceptionCode, o);
            if (put != null) {
                LOGGER.warn("exception code already override. exceptionCode = {}", exceptionCode);
            }

        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("exceptionClass not found, please check", e);
        } catch (MockException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("json is not correct format. must be {" +
                    "\"exceptionCode\": \"code\"" +
                    "\"exceptionClass\": \"fullClassName\"" +
                    "\"exceptionMessage\":\"exceptionMessage\"" +
                    "}");
        }

    }
}
