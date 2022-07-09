package org.yamikaze.unit.test.tree;

import org.yamikaze.unit.test.tree.InvokeTree.InvokeEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-07-06 16:43
 */
public class Profilers {

    private static final Logger LOGGER = LoggerFactory.getLogger(Profilers.class);

    /**
     * Profilers log enabled control in thread scope.
     */
    private static final ThreadLocal<Boolean> ENABLED_HOLDER = ThreadLocal.withInitial(() -> false);

    /**
     * Invoke tree for current invoke.
     */
    private static final ThreadLocal<InvokeTree> INVOKE_TREE = new ThreadLocal<>();

    /**
     * Remove current invoke tree.
     */
    public static void clear() {
        INVOKE_TREE.remove();
    }

    public static void startInvoke(String message) {
        InvokeEntry invokeEntry = InvokeTree.newInvokeEntry(message);

        InvokeTree invokeTree = INVOKE_TREE.get();

        if (invokeTree == null) {
            invokeTree = new InvokeTree();
            INVOKE_TREE.set(invokeTree);
            invokeTree.setRoot(invokeEntry);
            return;
        }

        invokeTree.enterInvokeEntry(invokeEntry);
    }

    public static void closed(boolean isMocked) {
        InvokeTree invokeTree = INVOKE_TREE.get();
        if (invokeTree == null) {
            LOGGER.error("invokeTree is null, can't closed.");
        } else {
            invokeTree.closed(isMocked, false);
        }

        if (isClosed()) {
            dump();
            clear();
        }
    }

    public static void closedWithException(boolean isMocked, boolean isException) {
        InvokeTree invokeTree = INVOKE_TREE.get();
        if (invokeTree == null) {
            LOGGER.error("invokeTree is null, can't closed.");
        } else {
            invokeTree.closed(isMocked, isException);
        }

        if (isClosed()) {
            dump();
            clear();
        }
    }

    public static boolean isClosed() {
        InvokeTree invokeTree = INVOKE_TREE.get();
        if (invokeTree != null) {
            return invokeTree.isClosed();
        }

        return false;
    }

    public static void dump() {
        InvokeTree invokeTree = INVOKE_TREE.get();
        if (invokeTree == null) {
            return;
        }

        invokeTree.dump();
    }

    /**
     * Enable invoke tree map.
     */
    public static void enable() {
        ENABLED_HOLDER.set(true);
    }

    public static boolean enabled() {
        return ENABLED_HOLDER.get();
    }

    public static void disable() {
        ENABLED_HOLDER.remove();
    }
}
