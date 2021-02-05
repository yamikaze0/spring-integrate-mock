package org.yamikaze.unit.test.junit;

import org.yamikaze.unit.test.junit.parameterized.InvokeParamMethod;
import org.yamikaze.unit.test.junit.parameterized.ParameterizedSource;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Supported junit4 parameterized test.
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 16:46
 */
public class JunitParameterizedRunner extends BlockJUnit4ClassRunner {

    public JunitParameterizedRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation,
                                                  boolean isStatic, List<Throwable> errors) {

        // Check test methods : must be void and no args.
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annotation);
        validate(methods, isStatic, errors);
    }

    public static void validate(List<FrameworkMethod> methods,
                                boolean isStatic, List<Throwable> errors) {
        // Check test methods : must be void and no args.
        for (FrameworkMethod eachTestMethod : methods) {

            ParameterizedSource isTemplateTest = eachTestMethod.getAnnotation(ParameterizedSource.class);
            if (isTemplateTest == null) {
                eachTestMethod.validatePublicVoidNoArg(isStatic, errors);
            } else {
                eachTestMethod.validatePublicVoid(isStatic, errors);
                if (eachTestMethod.getMethod().getParameterTypes().length == 0) {
                    errors.add(new Exception("Method " + eachTestMethod.getMethod().getName() + " should have parameters"));
                }
            }
        }
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        return createMethodInvoker(method, test);
    }

    public static Statement createMethodInvoker(FrameworkMethod method, Object test) {
        return new InvokeParamMethod(method, test);
    }

}
