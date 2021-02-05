package org.yamikaze.mock.agent;

import java.lang.instrument.Instrumentation;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-09-10 12:27
 */
public class AgentEntrance {

    public static void premain(String args, Instrumentation inst) {
        init(inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        init(inst);
    }

    private static void init(Instrumentation inst) {
        RuntimeAgent.init(inst);
    }
}
