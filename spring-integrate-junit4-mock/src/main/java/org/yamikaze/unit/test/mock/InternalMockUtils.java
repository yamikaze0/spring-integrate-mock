package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.answer.Answer;
import org.yamikaze.unit.test.mock.proxy.InvocationMethod;
import org.yamikaze.unit.test.tree.Profilers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodType;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-05-15 10:25
 */
public class InternalMockUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalMockUtils.class);
    public static final Object NO_MOCK = new Object();

    public static Object findMockResult(String className, String methodName, String methodDesc, Object[] args) throws Throwable {
        Class<?> clz = SpringJunitMockRunner.getModifiedClass(className);
        if (clz == null) {
            return NO_MOCK;
        }

        Class<?>[] paramTypes = generateParamTypes(methodDesc);

        InvocationMethod mi = new InvocationMethod();
        mi.setMethod(ClassUtils.findMethod(clz, methodName, paramTypes));
        mi.setTargetClass(clz);
        mi.setDeclaringClass(clz);
        mi.setArgs(args);
        mi.setStaticInvoke(true);

        RecordBehavior recordBehavior = RecordBehaviorList.INSTANCE.findRecordBehavior(mi);
        if (recordBehavior == null) {
            return NO_MOCK;
        }

        Answer answer = recordBehavior.getAnswer();
        if (answer == null) {
            return NO_MOCK;
        }
        Profilers.startInvoke(className.replace("/", ".") + "#" + methodName);
        Profilers.closed(true);
        Object mockResult = answer.answer(mi);
        LOGGER.info("internal mockito mock. key = {}#{}", className, methodName);
        if (mockResult instanceof Throwable) {
            throw (Throwable)mockResult;
        }

        return mockResult;
    }

    private static Class<?>[] generateParamTypes(String methodDesc) {
        MethodType methodType = MethodType.fromMethodDescriptorString(methodDesc, SpringJunitMockRunner.LOADER);
        return methodType.parameterArray();
    }
}
