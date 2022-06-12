package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.answer.Answer;
import org.yamikaze.unit.test.mock.argument.ArgumentMatcher;
import org.yamikaze.unit.test.mock.proxy.InvocationMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mock Match Record Behavior
 * @author qinluo
 * @version 1.0.0
 * @date 2019-10-31 18:07
 */
public class RecordBehavior {

    /**
     * Record Class
     */
    private Class<?> clz;

    /**
     * Record Method
     */
    private Method method;

    /**
     * Record Spring BeanName
     */
    private String beanName;

    /**
     * Match params
     */
    private boolean matchParams;

    /**
     * Match beanName
     */
    private boolean matchBeanName;

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public boolean getMatchBeanName() {
        return matchBeanName;
    }

    public void setMatchBeanName(boolean matchBeanName) {
        this.matchBeanName = matchBeanName;
    }

    private final List<ArgumentMatcher> argumentMatchers = new ArrayList<>();

    public boolean getMatchParams() {
        return matchParams;
    }

    public void addArgumentMatcher(ArgumentMatcher argumentMatcher) {
        if (argumentMatcher != null) {
            argumentMatchers.add(argumentMatcher);
        }
    }

    public void setMatchParams(boolean matchParams) {
        this.matchParams = matchParams;
    }

    private List<Answer> answers = new ArrayList<>();

    public synchronized Answer getAnswer() {
        if (answers.isEmpty()) {
            return null;
        }

        if (answers.size() == 1) {
            return answers.get(0);
        }

        return answers.remove(0);
    }

    public Class<?> getClz() {
        return clz;
    }

    public void setClz(Class<?> clz) {
        this.clz = clz;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void addAnswer(Answer answer) {
        this.answers.add(answer);
    }

    /**
     * cs:off
     */
    public boolean match(InvocationMethod invocation) {
        //不处理static mock的场景
        if (invocation.getStaticInvoke()) {
            return false;
        }

        //不是当前配置的类以及子类
        if (clz != invocation.getTargetClass() && !clz.isAssignableFrom(invocation.getTargetClass())) {
            return false;
        }

        Method method0 = invocation.getMethod();
        boolean methodMatched = matchMethod(method0);
        if (!methodMatched) {
            return false;
        }

        //参数值匹配
        if (matchParams) {
            boolean matched = matchParams(invocation.getArgs());
            if (!matched) {
                return false;
            }
        }

        if (!matchBeanName) {
            return true;
        }

        return beanName != null && Objects.equals(beanName, invocation.getBeanName());
    }

    protected boolean matchMethod(Method method0) {
        return ClassUtils.compareMethod(method0, this.method);
    }

    protected boolean matchParams(Object[] args) {
        if (args.length != argumentMatchers.size()) {
            return false;
        }

        for (int index = 0; index < args.length; index++) {
            ArgumentMatcher matcher = argumentMatchers.get(index);
            if (!matcher.matchArgument(args[index])) {
                return false;
            }
        }

        return true;
    }
}
