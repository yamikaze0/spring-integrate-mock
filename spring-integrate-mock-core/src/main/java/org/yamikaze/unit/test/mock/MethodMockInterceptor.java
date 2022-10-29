package org.yamikaze.unit.test.mock;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.yamikaze.unit.test.method.MethodUtils;
import org.yamikaze.unit.test.mock.answer.Answer;
import org.yamikaze.unit.test.mock.proxy.InvocationMethod;
import org.yamikaze.unit.test.spi.ExtensionFactory;
import org.yamikaze.unit.test.spi.JsonObjectMapperProxy;
import org.yamikaze.unit.test.tree.Profilers;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
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
    private static final Map<Method, MethodInvokeTime> METHOD_KEY = new ConcurrentHashMap<>(16);

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

    private boolean isProfiler(MethodInvokeTime mit) {
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

        MethodInvokeTime methodInvokeTime = METHOD_KEY.get(method);
        if (methodInvokeTime == null) {
            methodInvokeTime = generateMethodInvokeTime(invocation);
            METHOD_KEY.put(method, methodInvokeTime);
        }

        Class<?> targetClazz = ClassUtils.extractClosedUnProxyClass(invocation);
        // Ensure target class is subclass of actual class.
        if (targetClazz == null || !methodInvokeTime.getDeclaringClass().isAssignableFrom(targetClazz)) {
            targetClazz = methodInvokeTime.getDeclaringClass();
        }
        // thread-unsafe
        methodInvokeTime.setTargetClass(targetClazz);

        if (isProfiler(methodInvokeTime)) {
            Profilers.startInvoke(methodInvokeTime.getSimpleKey());
        }

        Object answerResult = findAnswerResult(invocation, methodInvokeTime);
        if (answerResult != NO_MOCK) {
            LOGGER.info("mockito mock. key = {}", methodInvokeTime.getKey());
            methodInvokeTime.mockInvoke();
            if (answerResult instanceof Throwable) {
                if (isProfiler(methodInvokeTime)) {
                    Profilers.closed(true);
                }
                throw (Throwable)answerResult;
            }

            if (answerResult instanceof OriginMockHolder) {
                Method pmethod = methodInvokeTime.getMethod();
                OriginMockHolder holder = (OriginMockHolder) answerResult;
                answerResult = JsonObjectMapperProxy.decode(holder.getJson(), pmethod.getGenericReturnType());
            }

            if (isProfiler(methodInvokeTime)) {
                Profilers.closed(true);
            }

            return answerResult;
        }

        long start = System.currentTimeMillis();

        Object proceed;

        try {
            proceed = invocation.proceed();
        } catch (Throwable e) {
            if (isProfiler(methodInvokeTime)) {
                Profilers.closedWithException(false, true);
            }
            throw e;
        }

        long end = System.currentTimeMillis();
        methodInvokeTime.realInvoke();

        if (isProfiler(methodInvokeTime)) {
            Profilers.closed(false);
        }

        afterPostpositionProcess(methodInvokeTime, proceed, invocation.getArguments());

        recordAndLog(methodInvokeTime, (end - start), proceed);

        return proceed;
    }

    private void afterPostpositionProcess(MethodInvokeTime methodInvokeTime, Object proceed, Object ...args) {
        if (PROCESSORS.isEmpty()) {
            return;
        }

        MethodInvokeTime mit = methodInvokeTime.copyOf();
        for (PostpositionProcessor processor : PROCESSORS) {
            processor.afterRealInvokeProcess(mit, proceed, args);
        }
    }

    private MethodInvokeTime generateMethodInvokeTime(MethodInvocation invocation) {
        MethodInvokeTime mit = new MethodInvokeTime();
        Method method = invocation.getMethod();

        Class<?> declaringClazz = ClassUtils.extractClosedDeclaringClass(method.getDeclaringClass(), method);
        Class<?> actualClazz = declaringClazz;

        // handle abstract methods
        // compatible interface methods, because interface is abstract and interface
        if (Modifier.isAbstract(declaringClazz.getModifiers()) && !declaringClazz.isInterface()) {
            actualClazz = invocation.getThis().getClass();
        }

        Class<?> targetClazz = ClassUtils.extractClosedUnProxyClass(invocation);
        // Ensure target class is subclass of actual class.
        if (targetClazz == null || !actualClazz.isAssignableFrom(targetClazz)) {
            targetClazz = actualClazz;
        }

        mit.setDeclaringClass(actualClazz);
        mit.setTargetClass(targetClazz);
        mit.setMethod(ClassUtils.findMethod(declaringClazz, method.getName(), method.getParameterTypes()));
        return mit;
    }

    private Object findAnswerResult(MethodInvocation invocation, MethodInvokeTime mit) {
        InvocationMethod mi = new InvocationMethod();
        mi.setProxy(invocation.getThis());
        mi.setMethod(invocation.getMethod());
        mi.setTargetClass(mit.getTargetClass());
        mi.setDeclaringClass(mit.getDeclaringClass());
        mi.setArgs(invocation.getArguments());
        mi.setBeanName(tryGetBeanName(invocation));

        mit.setCurrentInvocation(mi);

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

    private void recordAndLog(MethodInvokeTime mit, long escape, Object proceedResult) {
        String methodName = mit.getMethod().getName();

        //factoryBean就不打日志了
        if (FactoryBean.class.isAssignableFrom(mit.getDeclaringClass())) {
            return;
        }

        if (GlobalConfig.getEnableRealInvokeLog()) {
            LOGGER.info("[REAL-RESULT] escape = {}, class = {}#{}", escape, mit.getDeclaringClass().getName(), methodName);
            LOGGER.info("[REAL-RESULT] \t value = {}", JsonObjectMapperProxy.encode(proceedResult));
        }
    }

    static void clear() {
        METHOD_KEY.clear();
        Profilers.dump();
        Profilers.clear();
    }
}
