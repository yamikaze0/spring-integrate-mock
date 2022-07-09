package org.yamikaze.unittest.junit4;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.yamikaze.unit.test.check.Checker;
import org.yamikaze.unit.test.check.MethodDescriptor;
import org.yamikaze.unit.test.degrade.DegradeUtils;
import org.yamikaze.unit.test.handler.Handler;
import org.yamikaze.unit.test.handler.ThrowExceptionHandler;
import org.yamikaze.unit.test.mock.ClassUtils;
import org.yamikaze.unit.test.mock.MockContextUtils;
import org.yamikaze.unit.test.mock.event.Event;
import org.yamikaze.unit.test.mock.event.EventListener;
import org.yamikaze.unit.test.mock.event.TestFinishedEvent;
import org.yamikaze.unit.test.spi.ExtensionFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 14:07
 */
public class ExtensionRule implements TestRule {

    private static final ExtensionRule INSTANCE = new ExtensionRule();

    public static ExtensionRule getInstance() {
        return INSTANCE;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new ExtensionStatement(base, description);
    }

    private static class ExtensionStatement extends Statement {

        /**
         * Junit 相关属性
         */
        private final Statement baseStatement;

        private final Description description;

        /**
         * Checker List
         */
        private static final List<Checker> CHECKERS = new ArrayList<>(16);

        private static final Handler DEFAULT_EXCEPTION_HANDLER = new ThrowExceptionHandler();

        private static final List<EventListener> EVENT_LISTENERS = new ArrayList<>(16);

        /**
         * Handler List
         */
        private static final List<Handler> HANDLERS = new ArrayList<>(16);

        static {
            List<Checker> checkers = ExtensionFactory.getExtensions(Checker.class);
            CHECKERS.addAll(checkers);

            List<EventListener> listeners = ExtensionFactory.getExtensions(EventListener.class);
            EVENT_LISTENERS.addAll(listeners);

            List<Handler> handlers = ExtensionFactory.getExtensions(Handler.class);
            HANDLERS.addAll(handlers);
        }


        ExtensionStatement(Statement base, Description description) {
            this.baseStatement = base;
            this.description = description;
        }

        @Override
        public void evaluate() throws Throwable {
            MethodDescriptor descriptor = new MethodDescriptor();
            descriptor.setType(description.getTestClass());
            descriptor.setMethodName(description.getMethodName());
            descriptor.setMethod(ClassUtils.findMethod(description.getTestClass(), description.getMethodName()));
            descriptor.setfAnnotations(description.getAnnotations().toArray(new Annotation[0]));

            // prepare context
            MockContextUtils.prepare();

            //first checker
            touchChecker(descriptor);

            //pre handler
            preHandler(descriptor);

            try {
                baseStatement.evaluate();
            } catch (Throwable e) {
                exceptionHandle(e, descriptor);
            } finally {

                DegradeUtils.degradeVoid(() -> {
                    Event event = new TestFinishedEvent();
                    for (EventListener eventListener : EVENT_LISTENERS) {
                        eventListener.onEvent(event);
                    }
                });

                afterHandler(descriptor);

                // disable invoke tree log
                MockContextUtils.clear();
            }

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
    }
}
