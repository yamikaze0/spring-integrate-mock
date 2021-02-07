package org.yamikaze.unit.test.tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-07-06 16:39
 */
public class InvokeTree {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvokeTree.class);

    private long totalInvokeTime;

    private boolean hasException;

    private InvokeEntry root;
    
    private final long start;

    InvokeTree() {
        this.start = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return root.toString();
    }

    void closed(boolean isMocked, boolean isException) {
        
        closed0(isMocked, isException);
        
        //root is closed.
        if (root.closed) {
            this.totalInvokeTime = System.currentTimeMillis() - start;

        }
    }

    private void closed0(boolean isMocked, boolean isException) {
        if (root.closed) {
            //ignore
            return;
        }


        InvokeEntry unClosedEntry = root.getUnClosedEntry();
        if (unClosedEntry != null) {
            unClosedEntry.closeEntry(isMocked);
            unClosedEntry.setOccurredException(isException);
        }
    }

    public long getTotalInvokeTime() {
        return totalInvokeTime;
    }

    public void setTotalInvokeTime(long totalInvokeTime) {
        this.totalInvokeTime = totalInvokeTime;
    }

    public boolean getHasException() {
        return hasException;
    }

    public void setHasException(boolean hasException) {
        this.hasException = hasException;
    }

    public InvokeEntry getRoot() {
        return root;
    }

    void setRoot(InvokeEntry root) {
        this.root = root;
    }
    
    static InvokeEntry newInvokeEntry(String message) {
        return new InvokeEntry(message);
    }

    synchronized void enterInvokeEntry(InvokeEntry invokeEntry) {
        if (invokeEntry == null) {
            return;
        }

        InvokeEntry unClosedEntry = root.getUnClosedEntry();

        invokeEntry.setParent(unClosedEntry);
        unClosedEntry.getSubInvokeEntries().add(invokeEntry);
    }

    boolean isClosed() {
        return root.closed;
    }

    void dump() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("Invoke Tree Dump:\n");
        root.dump("", sb);

        String log = sb.toString();
        LOGGER.info(log);
    }

    static class InvokeEntry {


        private InvokeEntry parent;

        private List<InvokeEntry> subInvokeEntries = new ArrayList<>(16);

        private long invokeTime;

        private boolean isMocked;

        private boolean hasRealInvoke;

        private boolean closed;
        
        private final long start;

        private boolean occurredException;

        /**
         * Current Node message.
         */
        private final String message;

        InvokeEntry(String message) {
            this.message = message;
            this.start = System.currentTimeMillis();
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return message;
        }

        public void closeEntry(boolean isMocked) {
            //avoid second close.
            if (!this.closed) {
                this.isMocked = isMocked;
                this.hasRealInvoke = !isMocked;
                this.invokeTime = System.currentTimeMillis() - start;
            }

            this.closed = true;

        }

        public boolean getOccurredException() {
            return occurredException;
        }

        public void setOccurredException(boolean occurredException) {
            this.occurredException = occurredException;
        }

        public boolean getClosed() {
            return closed;
        }

        public InvokeEntry getParent() {
            return parent;
        }

        public void setParent(InvokeEntry parent) {
            this.parent = parent;
        }

        public List<InvokeEntry> getSubInvokeEntries() {
            return subInvokeEntries;
        }

        public void setSubInvokeEntries(List<InvokeEntry> subInvokeEntries) {
            this.subInvokeEntries = subInvokeEntries;
        }

        public long getInvokeTime() {
            return invokeTime;
        }

        public void setInvokeTime(long invokeTime) {
            this.invokeTime = invokeTime;
        }

        public boolean getMocked() {
            return isMocked;
        }

        public void setMocked(boolean mocked) {
            isMocked = mocked;
        }

        public boolean getHasRealInvoke() {
            return hasRealInvoke;
        }

        public void setHasRealInvoke(boolean hasRealInvoke) {
            this.hasRealInvoke = hasRealInvoke;
        }

        public InvokeEntry getUnClosedEntry() {
            if (this.closed) {
                return null;
            }

            if (subInvokeEntries.isEmpty()) {
                return this;
            }

            for (int i = subInvokeEntries.size() - 1; i >= 0; i--) {
                InvokeEntry unClosedEntry = subInvokeEntries.get(i).getUnClosedEntry();
                if (unClosedEntry != null) {
                    return unClosedEntry;
                }
            }

            return this;

        }

        private boolean hasMocked() {
            if (this.subInvokeEntries.isEmpty()) {
                return isMocked;
            }

            for (InvokeEntry invokeEntry : subInvokeEntries) {
                if (invokeEntry.hasMocked()) {
                    return true;
                }
            }

            return false;
        }

        private boolean hasNextBrother() {
            if (parent == null || parent.getSubInvokeEntries().isEmpty()) {
                return false;
            }

            return parent.getSubInvokeEntries().indexOf(this)
                    < parent.getSubInvokeEntries().size() - 1;
        }

        private boolean hasRealInvoke() {
            if (this.subInvokeEntries.isEmpty()) {
                return hasRealInvoke;
            }

            for (InvokeEntry invokeEntry : subInvokeEntries) {
                if (invokeEntry.hasRealInvoke()) {
                    return true;
                }
            }

            return false;
        }

        public void dump(String prefix, StringBuilder sb) {
            boolean isMocked0 = this.hasMocked();
            boolean isRealInvoke0 = this.hasRealInvoke();

            sb.append(prefix).append(message).append(" time = ").append(invokeTime).append("ms, mode = ");
            String mode = (isMocked0 && isRealInvoke0) ? "mix" : isMocked0 ? "mock" : "real invoke";
            sb.append(mode);


            if (occurredException) {
                sb.append(" occurredException");
            }
            sb.append("\n");


            if (this.subInvokeEntries.isEmpty()) {
                return;
            }


            for (InvokeEntry subInvokeEntry : subInvokeEntries) {
                String nextPrefix = generatePrefix(prefix, hasNextBrother());
                subInvokeEntry.dump(nextPrefix, sb);
            }
        }

        private String generatePrefix(String prefix, boolean i) {
            if (prefix == null || prefix.length() == 0) {
                return "|---";
            }

            if (prefix.length() == 4) {
                return (i ? "|" : " ") + "   |---";
            }

            int length = prefix.length() - 5;

            return prefix.substring(0, length) + (i ? " |" : "  ") + "   |---";
        }
    }
}
