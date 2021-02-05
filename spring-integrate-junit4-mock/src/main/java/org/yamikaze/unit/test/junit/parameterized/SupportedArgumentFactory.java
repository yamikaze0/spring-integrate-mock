package org.yamikaze.unit.test.junit.parameterized;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 17:39
 */
public interface SupportedArgumentFactory extends ArgumentFactory {

    /**
     * Check current ArgumentFactory is support this format.
     * @param fileLocation fileLocation
     * @return             if supported return true, else return false.
     */
    boolean supported(String fileLocation);
}
