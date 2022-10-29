package org.yamikaze.unit.test.mock;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.yamikaze.unit.test.method.MethodUtils;
import org.yamikaze.unit.test.mock.answer.Answer;
import org.yamikaze.unit.test.mock.proxy.MockInvocation;
import org.yamikaze.unit.test.spi.ExtensionFactory;
import org.yamikaze.unit.test.spi.JsonObjectMapperProxy;
import org.yamikaze.unit.test.tree.Profilers;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 11:25
 * cs:off
 */
public class MethodMockInterceptor implements MethodInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodMockInterceptor.class);

    /**
     * Current Test method info.
     */
    private static final Map<Method, InternalMethodInvocation> INVOCATIONS = new ConcurrentHashMap<>(16);

    /**
     * NO_MOCK object.
     */
    private static final Object NO_MOCK = new Object();

    /**
     * After real-invoke processors.
     */
    private static final List<PostpositionProcessor> PROCESSORS = new ArrayList<>(16);

    static {
        List<PostpositionProcessor> processors = ExtensionFactory.getExtensions(PostpositionProcessor.class);
        PROCESSORS.addAll(processors);
    }

    private boolean isProfiler(InternalMethodInvocation mit) {
        return Profilers.enabled()
                && !mit.getDeclaringClass().getName().contains("AbstractConfig")
                && !mit.getDeclaringClass().getName().contains("ServiceBean");
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (MethodUtils.isBaseMethod(method) || !GlobalConfig.isMockEnabled()) {
            return invocation.proceed();
        }

        InternalMethodInvocation internalInvocation = INVOCATIONS.get(method);
        if (internalInvocation == null) {
            internalInvocation = createInvocation(invocation);
            INVOCATIONS.put(method, internalInvocation);
        }

        Class<?> targetClazz = ClassUtils.extractClosedUnProxyClass(invocation);
        // Ensure target class is subclass of actual class.
        if (targetClazz == null || !internalInvocation.getDeclaringClass().isAssignableFrom(targetClazz)) {
            targetClazz = internalInvocation.getDeclaringClass();
        }
        // thread-safe
        internalInvocation.setTarget(targetClazz);

        if (isProfiler(internalInvocation)) {
            Profilers.startInvoke(internalInvocation.getSimpleKey());
        }

        Object answerResult = findAnswerResult(invocation, internalInvocation);
        if (answerResult != NO_MOCK) {
            LOGGER.info("mockito mock. key = {}", internalInvocation.getKey());
            internalInvocation.mockInvoked();
            if (answerResult instanceof Throwable) {
                if (isProfiler(internalInvocation)) {
                    Profilers.closed(true);
                }
                throw (Throwable)answerResult;
            }

            if (answerResult instanceof OriginMockHolder) {
                Method pmethod = internalInvocation.getMethod();
                OriginMockHolder holder = (OriginMockHolder) answerResult;
                answerResult = JsonObjectMapperProxy.decode(holder.getJson(), pmethod.getGenericReturnType());
            }

            if (isProfiler(internalInvocation)) {
                Profilers.closed(true);
            }

            return answerResult;
        }

        long start = System.currentTimeMillis();

        Object proceed;

        try {
            proceed = invocation.proceed();
        } catch (Throwable e) {
            if (isProfiler(internalInvocation)) {
                Profilers.closedWithException(false, true);
            }
            throw e;
        }

        long end = System.currentTimeMillis();
        internalInvocation.realInvoked();

        if (isProfiler(internalInvocation)) {
            Profilers.closed(false);
        }

        afterPostpositionProcess(internalInvocation, proceed, invocation.getArguments());

        recordAndLog(internalInvocation, (end - start), proceed);

        return proceed;
    }

    private void afterPostpositionProcess(InternalMethodInvocation methodInvokeTime, Object proceed, Object ...args) {
        if (PROCESSORS.isEmpty()) {
            return;
        }

        InternalMethodInvocation mit = methodInvokeTime.copyOf();
        for (PostpositionProcessor processor : PROCESSORS) {
            processor.afterRealInvokeProcess(mit, proceed, args);
        }
    }

    private InternalMethodInvocation createInvocation(MethodInvocation invocation) {
        InternalMethodInvocation mit = new InternalMethodInvocation();
        Method method = invocation.getMethod();

        Class<?> declaringClazz = ClassUtils.extractClosedDeclaringClass(method.getDeclaringClass(), method);
        Class<?> actualClazz = declaringClazz;

        // handle abstract methods
        // compatible interface methods, because interface is abstract and interface
        if (Modifier.isAbstract(declaringClazz.getModifiers()) && !declaringClazz.isInterface()) {
            actualClazz = invocation.getThis().getClass();
        }

        mit.setDeclaringClass(actualClazz);
        mit.setMethod(ClassUtils.findMethod(declaringClazz, method.getName(), method.getParameterTypes()));
        return mit;
    }

    private Object findAnswerResult(MethodInvocation invocation, InternalMethodInvocation mit) {
        MockInvocation mi = new MockInvocation();
        mi.setProxy(invocation.getThis());
        mi.setMethod(invocation.getMethod());
        mi.setTargetClass(mit.getTarget());
        mi.setDeclaringClass(mit.getDeclaringClass());
        mi.setArgs(invocation.getArguments());
        mi.setBeanName(tryGetBeanName(invocation));

        RecordBehavior recordBehavior = RecordBehaviorList.INSTANCE.findRecordBehavior(mi);
        if (recordBehavior == null) {
            return NO_MOCK;
        }

        Answer answer = recordBehavior.getAnswer();
        if (answer == null) {
            return NO_MOCK;
        }

        return answer.answer(mi);
    }

    private String tryGetBeanName(MethodInvocation invocation) {
        Object proxy = invocation.getThis();
        if (proxy != null) {
            return MockBeanNameAutoProxyCreator.findProxyClassBeanName(proxy.getClass());
        }

        return null;
    }

    private void recordAndLog(InternalMethodInvocation mit, long escape, Object proceedResult) {
        String methodName = mit.getMethod().getName();

        //factoryBean就不打日志了
        if (FactoryBean.class.isAssignableFrom(mit.getDeclaringClass())) {
            return;
        }

        if (GlobalConfig.getEnableRealInvokeLog()) {
            LOGGER.info("[REAL-RESULT] escape = {}, class = {}#{}", escape, mit.getTarget().getName(), methodName);
            LOGGER.info("[REAL-RESULT] \t value = {}", JsonObjectMapperProxy.encode(proceedResult));
        }
    }

    static void clear() {
        INVOCATIONS.clear();
        Profilers.dump();
        Profilers.clear();
    }
}
