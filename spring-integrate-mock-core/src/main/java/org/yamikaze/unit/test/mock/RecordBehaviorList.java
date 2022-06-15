package org.yamikaze.unit.test.mock;

import org.yamikaze.unit.test.mock.proxy.InvocationMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-10-31 18:07
 */
public class RecordBehaviorList {

    /**
     * Singleton
     */
    public static final RecordBehaviorList INSTANCE = new RecordBehaviorList();

    /**
     * Current unittest record behaviors
     */
    private final List<RecordBehavior> recordBehaviors = new ArrayList<>();

    /**
     * Current stack top record behavior
     */
    private RecordBehavior currentRecordBehavior;

    public RecordBehavior createRecordBehavior() {
        currentRecordBehavior = new RecordBehavior();
        synchronized (recordBehaviors) {
            recordBehaviors.add(0, currentRecordBehavior);
        }
        return currentRecordBehavior;
    }

    public List<RecordBehavior> getAllBehaviors() {
        synchronized (recordBehaviors) {
            List<RecordBehavior> internalBehaviors = new ArrayList<>(recordBehaviors);
            return Collections.unmodifiableList(internalBehaviors);
        }
    }

    public void addRecordBehavior(RecordBehavior recordBehavior) {
        if (recordBehavior == null) {
            throw new IllegalStateException("please first invoke when method!");
        }

        synchronized (recordBehaviors) {
            recordBehaviors.add(recordBehavior);
        }
    }

    public RecordBehavior getCurrentRecordBehavior() {
        if (currentRecordBehavior == null) {
            throw new IllegalStateException("please first invoke when method!");
        }

        return currentRecordBehavior;
    }

    public RecordBehavior findRecordBehavior(InvocationMethod invocation) {
        RecordBehavior behavior;
        RecordBehavior nearestBehavior = null;

        synchronized (recordBehaviors) {
            for (RecordBehavior recordBehavior : recordBehaviors) {
                //不匹配 执行下一个匹配
                if (!recordBehavior.match(invocation)) {
                    continue;
                }

                behavior = recordBehavior;
                if (nearestBehavior == null) {
                    nearestBehavior = recordBehavior;
                    continue;
                }

                //fix
                if (nearestBehavior == behavior) {
                    continue;
                }

                nearestBehavior = compare(nearestBehavior, behavior);
            }
        }

        return nearestBehavior;
    }

    private RecordBehavior compare(RecordBehavior nearestBehavior, RecordBehavior behavior) {
        Class<?> clz = nearestBehavior.getClz();
        Class<?> behaviorClz = behavior.getClz();
        if (clz == behaviorClz) {
            return nearestBehavior;
        }

        if (clz.isAssignableFrom(behaviorClz)) {
            return behavior;
        }

        return nearestBehavior;

    }

    public void clear() {
        currentRecordBehavior = null;
        recordBehaviors.clear();
    }
}
