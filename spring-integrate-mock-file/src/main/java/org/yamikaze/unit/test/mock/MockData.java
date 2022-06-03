package org.yamikaze.unit.test.mock;

import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2019/10/17
 */
public class MockData {
    private List<String> params;
    private String result;
    private Boolean matchParam;

    public Boolean getMatchParam() {
        return matchParam;
    }

    public void setMatchParam(Boolean matchParam) {
        this.matchParam = matchParam;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
