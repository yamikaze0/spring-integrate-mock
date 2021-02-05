package org.yamikaze.unit.test.mock;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-05 09:37
 */
public class VmUtils {

    public static int getVmPid() {

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        // format: "pid@hostname"
        String name = runtimeMXBean.getName();
        int indexOf = name.indexOf("@");

        //大于等于1才表示有pid
        if (indexOf >= 1) {
            return Integer.valueOf(name.substring(0, indexOf));
        }

        return 0;
    }

    private static int safeParse(String text) {

        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
