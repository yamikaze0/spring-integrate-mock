package org.yamikaze.unittest.junit5;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.LifecycleMethodExecutionExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamikaze.unit.test.check.Checker;
import org.yamikaze.unit.test.check.MethodDescriptor;
import org.yamikaze.unit.test.degrade.DegradeUtils;
import org.yamikaze.unit.test.handler.Handler;
import org.yamikaze.unit.test.handler.ThrowExceptionHandler;
import org.yamikaze.unit.test.mock.AgentProxy;
import org.yamikaze.unit.test.mock.EnhancerProxy;
import org.yamikaze.unit.test.mock.GlobalConfig;
import org.yamikaze.unit.test.mock.MockRunnerHelper;
import org.yamikaze.unit.test.mock.RecordBehaviorList;
import org.yamikaze.unit.test.mock.annotation.MockEnhance;
import org.yamikaze.unit.test.mock.event.Event;
import org.yamikaze.unit.test.mock.event.EventListener;
import org.yamikaze.unit.test.mock.event.TestFinishedEvent;
import org.yamikaze.unit.test.spi.ExtensionFactory;

import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author qinluo
 * @date 2022-06-11 22:58:50
 * @since 1.0.0
 */
public class Junit5MethodEachCallback implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, LifecycleMethodExecutionExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Junit5MethodEachCallback.class);

    /**
     * Store enhanced test-class.
     */
    private static final Map<Class<?>, Boolean> ENHANCED_MAP = new ConcurrentHashMap<>();

    /**
     * Handler List
     */
    private static final List<Handler> HANDLERS = new ArrayList<>(16);
    /**
     * Checker List
     */
    private static final List<Checker> CHECKERS = new ArrayList<>(16);

    private static final Handler DEFAULT_EXCEPTION_HANDLER = new ThrowExceptionHandler();

    private static final List<EventListener> EVENT_LISTENERS = new ArrayList<>(16);

    static {
        List<Checker> checkers = ExtensionFactory.getExtensions(Checker.class);
        CHECKERS.addAll(checkers);

        List<EventListener> listeners = ExtensionFactory.getExtensions(EventListener.class);
        EVENT_LISTENERS.addAll(listeners);

        List<Handler> handlers = ExtensionFactory.getExtensions(Handler.class);
        HANDLERS.addAll(handlers);
    }

    private final AtomicBoolean agentInitialized = new AtomicBoolean(false);

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        if (agentInitialized.compareAndSet(false, true)) {
            initAgent();
        }

        Class<?> testClass = extensionContext.getRequiredTestClass();
        List<Class<?>> beingEnhanceClasses = MockRunnerHelper.extraEnhanceClasses(testClass.getAnnotation(MockEnhance.class));

        if (ENHANCED_MAP.put(testClass, true) == null) {
            try {
                EnhancerProxy.enhanceClasses(beingEnhanceClasses);
            } catch (UnmodifiableClassException e) {
                LOGGER.error("enhance class occurred error", e);
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        // do nothing
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        MethodDescriptor descriptor = new MethodDescriptor();
        descriptor.setType(extensionContext.getRequiredTestClass());
        descriptor.setMethod(extensionContext.getRequiredTestMethod());
        descriptor.setMethodName(extensionContext.getRequiredTestMethod().getName());

        touchChecker(descriptor);
        preHandler(descriptor);

    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        MethodDescriptor descriptor = new MethodDescriptor();
        descriptor.setType(extensionContext.getRequiredTestClass());
        descriptor.setMethod(extensionContext.getRequiredTestMethod());
        descriptor.setMethodName(extensionContext.getRequiredTestMethod().getName());

        DegradeUtils.degradeVoid(() -> {
            Event event = new TestFinishedEvent();
            for (EventListener eventListener : EVENT_LISTENERS) {
                eventListener.onEvent(event);
            }
        });

        afterHandler(descriptor);
        RecordBehaviorList.INSTANCE.clear();
    }

    @Override
    public void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        MethodDescriptor descriptor = new MethodDescriptor();
        descriptor.setType(context.getRequiredTestClass());
        descriptor.setMethod(context.getRequiredTestMethod());
        descriptor.setMethodName(context.getRequiredTestMethod().getName());
        exceptionHandle(throwable, descriptor);
    }

    private void touchChecker(MethodDescriptor descriptor) {
        if (CHECKERS.isEmpty()) {
            return;
        }

        for (Checker checker : CHECKERS) {
            checker.check(descriptor);
        }
    }

    private void preHandler(MethodDescriptor descriptor) {
        for (Handler handler : HANDLERS) {
            DegradeUtils.degradeVoid(() -> handler.before(descriptor));
        }
    }

    private void afterHandler(MethodDescriptor descriptor) {
        for (Handler handler : HANDLERS) {
            DegradeUtils.degradeVoid(() -> handler.after(descriptor));
        }
    }

    private void exceptionHandle(Throwable throwable, MethodDescriptor descriptor) throws Throwable{
        for (Handler handler : HANDLERS) {
            handler.throwEx(descriptor, throwable);
        }

        if (HANDLERS.isEmpty()) {
            DEFAULT_EXCEPTION_HANDLER.throwEx(descriptor, throwable);
        }
    }

    private static void initAgent() {
        if (GlobalConfig.getUseAgentProxy()) {
            AgentProxy.initAgent();
        }
    }
}
