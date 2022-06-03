package org.yamikaze.unit.test.mock;

import java.io.Closeable;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-17 10:35
 */
public class IOUtils {

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                //NO-op
            }
        }
    }
}
