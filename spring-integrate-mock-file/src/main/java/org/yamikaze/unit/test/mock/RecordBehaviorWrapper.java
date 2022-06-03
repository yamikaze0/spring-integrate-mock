package org.yamikaze.unit.test.mock;


import org.yamikaze.unit.test.mock.argument.ArgumentMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-30 14:41
 */
public class RecordBehaviorWrapper {

    private String key;

    private List<ArgumentMatcher> argumentMatchers = new ArrayList<>(16);

    public RecordBehaviorWrapper(String key, List<ArgumentMatcher> argumentMatchers) {
        this.key = key;
        this.argumentMatchers.addAll(argumentMatchers);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<ArgumentMatcher> getArgumentMatchers() {
        return argumentMatchers;
    }

    public void setArgumentMatchers(List<ArgumentMatcher> argumentMatchers) {
        this.argumentMatchers = argumentMatchers;
    }

    public void addArgumentMatchers(List<ArgumentMatcher> argumentMatchers) {
        this.argumentMatchers.addAll(argumentMatchers);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RecordBehaviorWrapper)) {
            return false;
        }

        RecordBehaviorWrapper wrapper = (RecordBehaviorWrapper)obj;

        if (!Objects.equals(key, wrapper.key)) {
            return false;
        }

        if (this.argumentMatchers.size() != wrapper.argumentMatchers.size()) {
            return false;
        }

        if (this.argumentMatchers.size() == 0) {
            return true;
        }

        for (int i = 0; i < argumentMatchers.size(); i++) {
            if (!argumentMatchers.get(i).equals(wrapper.argumentMatchers.get(i))) {
                return false;
            }
        }

        return true;
    }
}
