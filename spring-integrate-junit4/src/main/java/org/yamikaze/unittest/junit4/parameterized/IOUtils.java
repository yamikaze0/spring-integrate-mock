package org.yamikaze.unittest.junit4.parameterized;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-17 14:41
 */
@SuppressWarnings("AlibabaClassNamingShouldBeCamel")
final class IOUtils {

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            // ignore.
        }
    }
}
