package org.yamikaze.unit.test.mock.answer;

import org.yamikaze.unit.test.mock.ClassUtils;
import org.yamikaze.unit.test.mock.RecordBehaviorList;
import org.yamikaze.unit.test.mock.StaticAccurateRecordBehavior;
import org.yamikaze.unit.test.mock.argument.ArgumentMatcher;
import org.yamikaze.unit.test.mock.argument.DefaultArgumentMatcher;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-05-15 11:54
 */
public class MethodAnswerCollector {

    private Class mockType;

    private String methodName;

    private List<Class> mockMethodParameterTypes = new ArrayList<>(16);

    private Object[] args;

    private boolean mockFinished;

    private boolean calledParams;

    private boolean calledTypes;

    public MethodAnswerCollector(Class mockType, String methodName) {
        this.mockType = mockType;
        this.methodName = methodName;
        if (methodName == null || methodName.trim().isEmpty()) {
            throw new IllegalArgumentException("method name must not be blank!");
        }
    }

    public MethodAnswerCollector nonParam() {
        checkFinished();

        mockMethodParameterTypes.clear();
        checkMethod(methodName);
        this.calledTypes = true;
        return this;
    }

    public MethodAnswerCollector types(Class ...types) {
        checkFinished();
        checkTypes(types);

        mockMethodParameterTypes.clear();

        if (types != null && types.length > 0) {
            checkMethod(methodName, types);
            Collections.addAll(mockMethodParameterTypes, types);
        }

        this.calledTypes = true;
        return this;
    }

    public MethodAnswerCollector param(Object... args) {
        if (!calledTypes) {
            throw new IllegalStateException("you must before invoke param invoke types");
        }

        checkFinished();

        this.args = args;
        this.calledParams = true;
        return this;
    }

    public void doNothing() {
        StaticAccurateRecordBehavior behavior = generateStaticRecordBehavior(true);

        behavior.addAnswer(new NoopAnswer());
        RecordBehaviorList.INSTANCE.addRecordBehavior(behavior);
    }

    private StaticAccurateRecordBehavior generateStaticRecordBehavior(boolean voidMethod) {
        if (!calledTypes) {
            throw new IllegalStateException("you must before invoke result invoke types");
        }

        checkFinished();

        this.mockFinished = true;
        Class[] paramTypes = mockMethodParameterTypes.toArray(new Class[0]);
        Method method = ClassUtils.findMethod(mockType, methodName, paramTypes);
        if (method == null) {
            throw new IllegalArgumentException("can't find method " + methodName
                    + "(" + ClassUtils.appendClasses(paramTypes, true) + ") from class " + mockType.getName());
        }

        if (method.getReturnType() != void.class && voidMethod) {
            throw new IllegalStateException("method " + method.getName() + " return type is not void type, please invoke result");
        }

        if (method.getReturnType() == void.class && !voidMethod) {
            throw new IllegalStateException("method " + method.getName() + " return type is void type, please invoke doNothing");
        }

        StaticAccurateRecordBehavior behavior = new StaticAccurateRecordBehavior();
        behavior.setClz(mockType);
        behavior.setMethod(method);
        behavior.setMatchParams(this.calledParams);
        List<ArgumentMatcher> argumentMatchers = buildArgumentMatcher(args, method);
        if (!argumentMatchers.isEmpty()) {
            argumentMatchers.forEach(behavior::addArgumentMatcher);
        }
        return behavior;
    }

    public void result(Object result, Object ...results) {
        StaticAccurateRecordBehavior behavior = generateStaticRecordBehavior(false);

        behavior.addAnswer(new ReturnValueAnswer(result));

        if (results != null && results.length > 0) {
            for (Object r : results) {
                behavior.addAnswer(new ReturnValueAnswer(r));
            }
        }

        RecordBehaviorList.INSTANCE.addRecordBehavior(behavior);
    }

    private List<ArgumentMatcher> buildArgumentMatcher(Object[] args, Method method) {
        if (args == null || args.length == 0) {
            return new ArrayList<>(0);
        }

        List<ArgumentMatcher> argumentMatchers = new ArrayList<>(args.length);
        int index = 0;
        for (Object arg : args) {
            argumentMatchers.add(new DefaultArgumentMatcher(arg, method, index++));
        }

        return argumentMatchers;
    }

    private void checkTypes(Class[] types) {
        if (types == null || types.length == 0) {
            return;
        }

        for (Class type : types) {
            if (type == null) {
                throw new IllegalArgumentException("type must not be null!");
            }
        }
    }

    private void checkFinished() {
        if (this.mockFinished) {
            throw new IllegalStateException("mock record is finished, please invoke mockMethod start new mock record.");
        }
    }


    private void checkMethod(String method, Class... paramTypes) {
        Method[] declaredMethods = this.mockType.getDeclaredMethods();
        boolean nonParams = paramTypes == null || paramTypes.length == 0;
        Method matchedMethod = null;
        for (Method method0 : declaredMethods) {
            if (!method.equals(method0.getName())) {
                continue;
            }

            // non-args method.
            if (nonParams) {
                if (method0.getParameterTypes().length != 0) {
                    continue;
                }
                matchedMethod = method0;
                break;
            }

            if (method0.getParameterCount() != paramTypes.length) {
                continue;
            }

            Class<?>[] parameterTypes0 = method0.getParameterTypes();


            for (int i = 0; i < method0.getParameterCount(); i++) {
                if (paramTypes[i] != parameterTypes0[i]) {
                    break;
                }
            }

            matchedMethod = method0;
            break;
        }

        if (matchedMethod == null) {
            throw new IllegalStateException("can't find method " + method + " from class " + this.mockType.getName());
        }

    }

}
