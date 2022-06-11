package org.yamikaze.unittest.junit4.parameterized;

import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 15:00
 */
public interface ArgumentFactory {

    /**
     * Load arguments from fileLocation.
     * @param fileLocation fileLocation.
     * @return             a list of arguments.
     */
    List<Arguments> loadArguments(String fileLocation);
}
