package org.yamikaze.unit.test.mock;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamikaze.unit.test.handler.Handler;
import org.yamikaze.unit.test.mock.annotation.Mock;
import org.yamikaze.unit.test.mock.annotation.Mocks;
import org.yamikaze.unit.test.mock.answer.CodeAnswer;
import org.yamikaze.unit.test.mock.config.MockConfig;
import org.yamikaze.unit.test.tree.Profilers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 15:04
 */
public class MockHandler implements Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockHandler.class);

    @Override
    public void before(Statement statement, Description description) {
        MethodMockInterceptor.clear();
        String message = description.getClassName() + "#" + description.getMethodName();
        Profilers.startInvoke(message);

        Mock mock = description.getAnnotation(Mock.class);
        Mocks mocks = description.getAnnotation(Mocks.class);
        if (mock == null && mocks == null) {
            return;
        }

        List<MockConfig> mockConfigs = new ArrayList<>(16);

        if (mock != null) {
            mockConfigs.add(parseConfig(mock));
        }

        mockConfigs.addAll(parseMocks(mocks));

        /*
         * Process special
         * @Mock(clz = UserAdapter.class, method =  "getUserById" mockData = true, dataKey = "mock-getUserById01")
         * @Mock(clz = UserAdapter.class, method =  "getUserById" mockData = true, dataKey = "mock-getUserById02")
         *
         * Result is:
         *
         * RecordBehavior
         *     - clz: UserAdapter.class
         *     - method: getUserById
         *     - answers:
         *         - CodeAnswer: mock-getUserById01
         *         - CodeAnswer: mock-getUserById02
         *
         * Result not:
         *
         * RecordBehavior
         *     - clz: UserAdapter.class
         *     - method: getUserById
         *     - answers:
         *         - CodeAnswer: mock-getUserById01
         * RecordBehavior
         *     - clz: UserAdapter.class
         *     - method: getUserById
         *     - answers:
         *         - CodeAnswer: mock-getUserById02
         *
         */
        Map<Method, RecordBehavior> methodRecordBehaviorMap = new HashMap<>(64);
        if (!mockConfigs.isEmpty()) {
            mockConfigs.forEach(p -> {
                List<Method> candidateMethods = ClassUtils.getCandidateMethods(p.getMockClass(), p.getMockMethodPattern());
                if (candidateMethods.isEmpty()) {
                    return;
                }

                candidateMethods.forEach(method -> {
                    RecordBehavior recordBehavior = methodRecordBehaviorMap.get(method);
                    if (recordBehavior == null) {
                        recordBehavior = new AccurateRecordBehavior();
                        methodRecordBehaviorMap.put(method, recordBehavior);
                        recordBehavior.setClz(p.getMockClass());
                        recordBehavior.setMethod(method);
                        RecordBehaviorList.INSTANCE.addRecordBehavior(recordBehavior);
                    }

                    String code = p.getDataKey();
                    boolean exception = p.isMockException();
                    if (exception) {
                        code = p.getMockExceptionCode();
                        boolean useDataKey = p.getDataKey() != null
                                && (code == null || ExceptionCodeFactory.COMMON_CODE.equals(code));
                        if (useDataKey) {
                            code = p.getDataKey();
                        }
                    }

                    recordBehavior.addAnswer(new CodeAnswer(exception, code));

                });
            });
        }
    }

    @Override
    public void after(Statement statement, Description description) {
        Profilers.closed(false);
        MethodMockInterceptor.clear();
    }


    @Override
    public void throwEx(Statement statement, Description description, Throwable throwable) {

    }

    private MockConfig parseConfig(Mock mock) {
        MockConfig config = new MockConfig();
        config.setDataKey(mock.dataKey());
        config.setMockClass(mock.clz());
        config.setMockData(mock.mockData());
        config.setMockException(mock.mockException());
        config.setMockMethodPattern(mock.method());
        config.setMockExceptionCode(mock.exceptionCode());
        return config;
    }

    private List<MockConfig> parseMocks(Mocks mocks) {
        List<MockConfig> mockConfigs = new ArrayList<>(16);
        if (mocks != null) {
            Mock[] mockList = mocks.value();
            for (Mock m : mockList) {
                mockConfigs.add(parseConfig(m));
            }
        }

        return mockConfigs;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj) instanceof MockHandler;
    }
}
