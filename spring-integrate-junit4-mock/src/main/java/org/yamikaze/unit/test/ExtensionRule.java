package org.yamikaze.unit.test;

import org.yamikaze.unit.test.check.Checker;
import org.yamikaze.unit.test.degrade.DegradeUtils;
import org.yamikaze.unit.test.handler.Handler;
import org.yamikaze.unit.test.handler.ThrowExceptionHandler;
import org.yamikaze.unit.test.mock.RecordBehaviorList;
import org.yamikaze.unit.test.mock.TestContext;
import org.yamikaze.unit.test.mock.event.Event;
import org.yamikaze.unit.test.mock.event.EventListener;
import org.yamikaze.unit.test.mock.event.TestFinishedEvent;
import org.yamikaze.unit.test.spi.ExtensionFactory;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

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

    /**
     * extension with checker and handler
     */
    public static void registerHandler(Handler handler) {
        ExtensionStatement.registerHandler(handler);
    }

    public static void registerChecker(Checker checker) {
        ExtensionStatement.registerChecker(checker);
    }

    public static void registerExceptionHandler(Handler handler) {
        ExtensionStatement.registerExceptionHandler(handler);
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

        private static final List<Handler> EXCEPTION_HANDLER = new ArrayList<>(16);

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
            //first checker
            touchChecker();

            TestContext.setTestInfo(description.getClassName(), description.getMethodName());

            //pre handler
            preHandler();

            try {
                baseStatement.evaluate();
            } catch (Throwable e) {
                exceptionHandle(e);
            } finally {

                DegradeUtils.degradeVoid(() -> {
                    Event event = new TestFinishedEvent();
                    for (EventListener eventListener : EVENT_LISTENERS) {
                        eventListener.onEvent(event);
                    }
                });

                afterHandler();
                EXCEPTION_HANDLER.clear();
                RecordBehaviorList.INSTANCE.clear();
            }

        }

        private void touchChecker() {
            if (CHECKERS.isEmpty()) {
                return;
            }

            for (Checker checker : CHECKERS) {
                checker.check(baseStatement, description);
            }
        }

        private void preHandler() {
            for (Handler handler : HANDLERS) {
                DegradeUtils.degradeVoid(() -> handler.before(baseStatement, description));
            }
        }

        private void afterHandler() {
            for (Handler handler : HANDLERS) {
                DegradeUtils.degradeVoid(() -> handler.after(baseStatement, description));
            }
        }

        private void exceptionHandle(Throwable throwable) throws Throwable{
            for (Handler handler : HANDLERS) {
                handler.throwEx(baseStatement, description, throwable);
            }

            if (EXCEPTION_HANDLER.isEmpty()) {
                DEFAULT_EXCEPTION_HANDLER.throwEx(baseStatement, description, throwable);
                return;
            }

            for (Handler handler : EXCEPTION_HANDLER) {
                handler.throwEx(baseStatement, description, throwable);
            }
        }

        public static void registerHandler(Handler handler) {
            HANDLERS.remove(handler);
            HANDLERS.add(handler);
        }

        public static void registerExceptionHandler(Handler handler) {
            EXCEPTION_HANDLER.remove(handler);
            EXCEPTION_HANDLER.add(handler);
        }

        public static void registerChecker(Checker checker) {
            CHECKERS.remove(checker);
            CHECKERS.add(checker);
        }
    }
}
