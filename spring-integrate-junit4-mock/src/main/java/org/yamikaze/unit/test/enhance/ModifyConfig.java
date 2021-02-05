package org.yamikaze.unit.test.enhance;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-07-22 16:45
 */
public class ModifyConfig {

    private String clzName;

    private boolean needModify;

    public ModifyConfig(String clzName, boolean needModify) {
        this.clzName = clzName;
        this.needModify = needModify;
    }

    public String getClzName() {
        return clzName;
    }

    public void setClzName(String clzName) {
        this.clzName = clzName;
    }

    public boolean getNeedModify() {
        return needModify;
    }

    public void setNeedModify(boolean needModify) {
        this.needModify = needModify;
    }
}
