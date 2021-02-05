package org.yamikaze.mock.agent;

import java.lang.instrument.Instrumentation;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-09-10 12:18
 */
public class RuntimeAgent {

    private static RuntimeAgent instance = null;

    private Instrumentation inst;

    private RuntimeAgent(Instrumentation inst) {
        this.inst = inst;
    }

    public boolean supportTransferClass() {
        return inst != null && inst.isRetransformClassesSupported();
    }

    public boolean supportModifyClass() {
        return inst != null && inst.isRedefineClassesSupported();
    }

    public void setInst(Instrumentation inst) {
        this.inst = inst;
    }

    static void init(Instrumentation inst) {
        if (inst == null) {
            throw new IllegalArgumentException("inst must not be null");
        }

        instance = new RuntimeAgent(inst);
    }

    public static RuntimeAgent getInstance() {
        return instance;
    }

    public Instrumentation getInst() {
        return inst;
    }
}
