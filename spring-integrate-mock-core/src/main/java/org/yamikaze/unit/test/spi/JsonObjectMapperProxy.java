package org.yamikaze.unit.test.spi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.yamikaze.unit.test.mock.ClassUtils;
import org.yamikaze.unit.test.mock.GsonDateTypeAdapter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

/**
 * @author qinluo
 * @date 2022-06-11 22:06:32
 * @since 1.0.0
 */
public class JsonObjectMapperProxy implements JsonObjectMapper {

    /**
     * The default instance.
     */
    private static final JsonObjectMapperProxy INSTANCE = new JsonObjectMapperProxy();
    private static final String USE_FASTJSON = "use.fastjson";
    private static final String FASTJSON_NAME = "com.alibaba.fastjson.JSON";
    private static final String FASTJSON_ENCODE = "toJSONString";
    private static final String FASTJSON_DECODE = "parseObject";
    public static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Date.class, new GsonDateTypeAdapter()).create();

    private Method encodeMethod;
    private Method decodeMethod;
    private Object delegateObject;
    private JsonObjectMapper delegate;


    @Override
    public String serialization(Object obj) {
        if (delegate != null) {
            return delegate.serialization(obj);
        }

        if (encodeMethod != null) {
            return (String) ClassUtils.invoke(encodeMethod, delegateObject, obj);
        }

        return GSON_PRETTY.toJson(obj);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialization(String json, Type type) {
        if (delegate != null) {
            return delegate.deserialization(json, type);
        }

        if (decodeMethod != null) {
            return (T) ClassUtils.invoke(decodeMethod, delegateObject, json, type);
        }

        return GSON_PRETTY.fromJson(json, type);
    }

    public JsonObjectMapperProxy() {
        List<JsonObjectMapper> extensions = ExtensionFactory.getExtensions(JsonObjectMapper.class);
        if (!extensions.isEmpty()) {
            delegate = extensions.get(0);
            return;
        }


        Class<?> fastjson = ClassUtils.initialization(FASTJSON_NAME, JsonObjectMapperProxy.class.getClassLoader());
        if (fastjson != null && Boolean.getBoolean(USE_FASTJSON)) {
            delegateObject = null;
            encodeMethod = ClassUtils.findMethod(fastjson, FASTJSON_ENCODE, Object.class);
            decodeMethod = ClassUtils.findMethod(fastjson, FASTJSON_DECODE, String.class, Class.class);
        }
    }

    /**
     * Static proxy.
     */
    public static String encode(Object obj) {
        return INSTANCE.serialization(obj);
    }

    public static <T> T decode(String json, Type type) {
        return INSTANCE.deserialization(json, type);
    }
}
