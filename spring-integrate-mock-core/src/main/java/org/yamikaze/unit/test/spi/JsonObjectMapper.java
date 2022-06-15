package org.yamikaze.unit.test.spi;

import java.lang.reflect.Type;

/**
 * @author qinluo
 * @date 2022-06-11 22:03:16
 * @since 1.0.0
 */
public interface JsonObjectMapper {

    /**
     * Serialize obj to json.
     *
     * @param obj obj
     * @return    json string
     */
    String serialization(Object obj);


    /**
     * Deserialization string as object.
     *
     * @param json json string
     * @param type object type.
     * @param <T>  parameterized type
     * @return     object.
     */
    <T> T deserialization(String json, Type type);
}
