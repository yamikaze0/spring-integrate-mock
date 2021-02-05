package org.yamikaze.unit.test.mock;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.yamikaze.unit.test.method.MethodUtils;
import org.yamikaze.unit.test.mock.answer.Answer;
import org.yamikaze.unit.test.mock.proxy.InvocationMethod;
import org.yamikaze.unit.test.spi.ExtensionFactory;
import org.yamikaze.unit.test.tree.Profilers;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static final Map<Method, MethodInvokeTime> METHOD_KEY = new HashMap<>(16);

    /**
     * NO_MOCK object.
     */
    private static final Object NO_MOCK = new Object();

    public static final Gson GSON =  new GsonBuilder().registerTypeAdapter(Date.class,new GsonDateTypeAdapter()).create();

    /**
     * After real-invoke processors.
     */
    private static final List<PostpositionProcessor> PROCESSORS = new ArrayList<>(16);

    static {
        List<PostpositionProcessor> processors = ExtensionFactory.getExtensions(PostpositionProcessor.class);
        PROCESSORS.addAll(processors);
    }

    public static void addPostpositionProcessor(PostpositionProcessor processor) {
        if (processor == null) {
            return;
        }

        PROCESSORS.remove(processor);
        PROCESSORS.add(processor);
    }

    private boolean isProfiler(MethodInvokeTime mit) {
        return !mit.getDeclaringClass().getName().contains("AbstractConfig")
                && !mit.getDeclaringClass().getName().contains("ServiceBean");
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        if (MethodUtils.isBaseMethod(method) || !GlobalConfig.getSwitch()) {
            return invocation.proceed();
        }

        MethodInvokeTime methodInvokeTime = METHOD_KEY.get(method);
        if (methodInvokeTime == null) {
            methodInvokeTime = generateMethodInvokeTime(invocation);
            METHOD_KEY.put(method, methodInvokeTime);
        }

        if (isProfiler(methodInvokeTime)) {
            Profilers.startInvoke(methodInvokeTime.getSimpleKey());
        }

        Object answerResult = findAnswerResult(invocation, methodInvokeTime);
        if (answerResult != NO_MOCK) {
            LOGGER.info("mockito mock. key = {}", methodInvokeTime.getKey());
            methodInvokeTime.mockInvoke();
            if (answerResult instanceof Throwable) {
                throw (Throwable)answerResult;
            }

            if (answerResult instanceof OriginMockHolder) {
                Method pmethod = methodInvokeTime.getMethod();
                OriginMockHolder holder = (OriginMockHolder) answerResult;
                answerResult = GSON.fromJson(holder.getJson(), pmethod.getGenericReturnType());
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

        mit.setDeclaringClass(actualClazz);
        mit.setMethod(ClassUtils.findMethod(declaringClazz, method.getName(), method.getParameterTypes()));
        return mit;
    }

    private Object findAnswerResult(MethodInvocation invocation, MethodInvokeTime mit) {
        InvocationMethod mi = new InvocationMethod();
        mi.setProxy(invocation.getThis());
        mi.setMethod(invocation.getMethod());
        mi.setTargetClass(mit.getDeclaringClass());
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
        String className = mit.getDeclaringClass().getSimpleName();
        String methodName = mit.getMethod().getName();

        //factoryBean就不打日志了
        if (FactoryBean.class.isAssignableFrom(mit.getDeclaringClass())) {
            return;
        }

        if (GlobalConfig.getEnableRealInvokeLog()) {
            LOGGER.info("[REAL-RESULT] escape = {}, class = {}#{}", escape, mit.getDeclaringClass().getName(), methodName);
            LOGGER.info("[REAL-RESULT] \t value = {}", JSON.toJSONString(proceedResult));
        }

        if (GlobalConfig.getEnableUsageLog()) {
            logMockConfig(mit, className, proceedResult);
        }
    }

    private void logMockConfig(MethodInvokeTime mit, String className, Object proceedResult) {
        String methodName = mit.getMethod().getName();
        String sb = "@Mock(clz = " + className +
                ".class, method = \"" + methodName + "\",dataKey = \"mock-" + methodName + "\")";
        LOGGER.info("if you need mock, please paste {}", sb);
        LOGGER.info("[REAL-RESULT] \t DataCodeFactory.register(\"mock-{}\", OriginMockHolder.newInstance(\"{}\"))", methodName,  JSON.toJSONString(proceedResult));

    }

    static void clear() {
        METHOD_KEY.clear();


        Profilers.dump();
        Profilers.clear();

    }
}
