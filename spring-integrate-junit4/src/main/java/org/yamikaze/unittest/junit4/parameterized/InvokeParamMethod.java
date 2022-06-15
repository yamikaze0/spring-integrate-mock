package org.yamikaze.unittest.junit4.parameterized;

import org.junit.Test;
import org.junit.Test.None;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 14:50
 */
public class InvokeParamMethod extends InvokeMethod {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvokeParamMethod.class);

    private final FrameworkMethod testMethod;
    private final Object target;

    public InvokeParamMethod(FrameworkMethod testMethod, Object target) {
        super(testMethod, target);
        this.testMethod = testMethod;
        this.target = target;
    }

    @Override
    public void evaluate() throws Throwable {
        Class<? extends Throwable> expectException = getExpectThrowable();
        ParameterizedSource template = testMethod.getAnnotation(ParameterizedSource.class);
        // If test method has no @ParameterizedSource, it's a normal test method.
        if (template == null) {
            super.evaluate();
            return;
        }

        String fileLocation = template.value();
        String name = testMethod.getName();

        if (fileLocation.trim().length() == 0) {
            throw new IllegalStateException("test method " + name
                    + "'s ParameterizedSource.fileLocation must not be null");
        }

        ArgumentFactory argumentFactory = ArgumentService.getArgumentFactory();
        List<Arguments> arguments = argumentFactory.loadArguments(fileLocation);

        if (arguments == null || arguments.isEmpty()) {
            throw new IllegalArgumentException("load arguments from " + fileLocation + " is empty");
        }

        LOGGER.info("{} execute has {} testcase", name, arguments.size());

        int testcaseIndex = 0;
        int successTestCase = 0;
        int failTestCase = 0;

        Class<?>[] parameterTypes = testMethod.getMethod().getParameterTypes();
        Annotation[][] parameterAnnotations = testMethod.getMethod().getParameterAnnotations();

        ParameterDescriptor[] parameterContexts = assemblyParamContext(parameterTypes, parameterAnnotations);

        for (Arguments argument : arguments) {
            LOGGER.info("start execute testcase {}", testcaseIndex);
            try {
                Object[] args;
                if (argument instanceof ObjectArguments) {
                    args = ((ObjectArguments)argument).getActualArgs();
                } else {
                    args = DefaultParamParser.getInstance().parse(argument, name, parameterContexts);
                }

                testMethod.invokeExplosively(target, args);
                successTestCase++;
            } catch (Throwable e) {
                if (expectException != None.class && expectException.isInstance(e)) {
                    successTestCase++;
                    continue;
                } else {
                    failTestCase++;
                    LOGGER.error("execute {} testcase : {} fail, arguments : {}", name, testcaseIndex, argument);
                    LOGGER.error("fail reason is ", e);
                }

            }
            LOGGER.info("end execute testcase {}", testcaseIndex);
            testcaseIndex++;
        }

        LOGGER.info("execute {}, total testcase : {}, success : {}, fail : {}", name, testcaseIndex, successTestCase, failTestCase);
        if (testcaseIndex != successTestCase) {
            String failMsg = "parameterized test execute fail. total case : " + testcaseIndex + " success case : " + successTestCase
                    + " fail case : " + failTestCase;
            throw new IllegalStateException(failMsg);
        }
    }

    private ParameterDescriptor[] assemblyParamContext(Class<?>[] parameterTypes, Annotation[][] parameterAnnotations) {
        ParameterDescriptor[] contexts = new ParameterDescriptor[parameterTypes.length];

        int index = 0;
        for (Class<?> type : parameterTypes) {
            contexts[index] = new ParameterDescriptor(type, parameterAnnotations[index]);
            index++;
        }

        return contexts;
    }

    private Class<? extends Throwable> getExpectThrowable() {
        Test annotation = testMethod.getAnnotation(Test.class);
        return annotation.expected();
    }
}
