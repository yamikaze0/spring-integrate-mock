package org.yamikaze.unit.test.junit.parameterized;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 15:01
 */
public class ArgumentService {

    public static ArgumentFactory getArgumentFactory() {
        return new ArgumentFactoryProxy();
    }
}
