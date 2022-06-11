package org.yamikaze.unit.test.mock;

import com.sun.tools.attach.VirtualMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-09-15 19:27
 */
public class AgentProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentProxy.class);

    private static final String AGENT_PATH = "common-java-agent-1.0.0.jar";
    private static final String AGENT_HOLDER = "org.yamikaze.common.agent.RuntimeInstHolder";
    private static final String AGENT_HOLDER_INST = "getInstance";
    private static final String AGENT_METHOD = "getInst";

    private static volatile boolean init;
    private static volatile boolean attached;
    private static volatile VirtualMachine attachedVm;
    private static File agentFile = null;

    static {
        registerCleaner();
    }

    private static void registerCleaner() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                detach();
            } catch (Exception e) {
                LOGGER.error("clean agent proxy occurred error", e);
            }
        }));
    }

    public static void initAgent() {
        if (init) {
            return;
        }

        try {
            int pid = VmUtils.getVmPid();
            if (pid <= 0) {
                return;
            }

            URL agentUrl = AgentProxy.class.getClassLoader().getResource(AGENT_PATH);
            if (agentUrl == null) {
                LOGGER.warn("Can't agent jar in class path {}", AGENT_PATH);
                return;
            }

            agentFile = File.createTempFile("mock-agent", "jar");
            copy(agentUrl.openStream(), new FileOutputStream(agentFile));
            LOGGER.info("copy agent jar to temp file {}", agentFile.getAbsolutePath());

            attachedVm = VirtualMachine.attach(String.valueOf(pid));
            attachedVm.loadAgent(agentFile.getAbsolutePath());
            LOGGER.info("attach target vm, load agent {} success", AGENT_PATH);
            attached = true;
            init = true;
        } catch (Exception e) {
            LOGGER.error("attach to target vm occurred error", e);
            attached = false;
            init = true;
            // cs:off
            try {
                if (attachedVm != null) {
                    attachedVm.detach();
                }
            } catch (Exception ee) {
                // ignore
            }
        }

    }

    public static boolean isAttached() {
        return attached;
    }

    public static void detach() {
        if (attached) {
            try {
                attachedVm.detach();
            } catch (Exception e) {
                //
            }
        }

        if (agentFile != null) {
            LOGGER.info("delete agent temp file {}", agentFile.getAbsolutePath());
            agentFile.delete();
        }
    }

    public static Instrumentation getInst() {
        if (!attached) {
            return null;
        }

        try {
            Class<?> hc = Class.forName(AGENT_HOLDER);
            Method agentMethod = hc.getDeclaredMethod(AGENT_HOLDER_INST);
            Object holder = agentMethod.invoke(null);

            Method agentGet = hc.getDeclaredMethod(AGENT_METHOD);
            return (Instrumentation)agentGet.invoke(holder);

        } catch (Exception e) {
            LOGGER.error("getInst occurred error ", e);
        }

        return null;
    }

    public static boolean supportRetransform(Instrumentation inst) {
        return inst != null && inst.isRedefineClassesSupported() && inst.isRetransformClassesSupported();
    }

    public static boolean supportRetransformClass(Instrumentation inst, Class<?> clz) {
        return supportRetransform(inst) && inst.isModifiableClass(clz);
    }

    /**
     *  Copy methods from Apache common.io IOUtils
     *  cs:off
     */


    private static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int)count;
    }

    private static long copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        return copyLarge(input, output, new byte[bufferSize]);
    }

    private static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, 4096);
    }

    private static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count;
        int n;
        for(count = 0L; -1 != (n = input.read(buffer)); count += (long)n) {
            output.write(buffer, 0, n);
        }

        return count;
    }

}
