package org.yamikaze.unit.test.mock;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-10-15 11:51
 */
public class OriginMockHolder {

    /**
     * 原始json
     */
    private String json;

    /**
     * 是否exception
     */
    private boolean exception;

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public boolean getException() {
        return exception;
    }

    public void setException(boolean exception) {
        this.exception = exception;
    }

    public static OriginMockHolder newInstance(String json) {
        return new OriginMockHolder(json, false);
    }

    public OriginMockHolder(String json) {
        this(json, false);
    }

    public OriginMockHolder(String json, boolean exception) {
        this.json = json;
        this.exception = exception;
    }
}
