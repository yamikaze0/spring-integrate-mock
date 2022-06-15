package org.yamikaze.unittest.junit4.parameterized.converter;

import java.text.SimpleDateFormat;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 18:19
 */
public class DateParamConverter implements ParamConverter {

    @Override
    public Object convert(String val) throws Throwable {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        return simpleDateFormat.parse(val.trim());
    }
}
