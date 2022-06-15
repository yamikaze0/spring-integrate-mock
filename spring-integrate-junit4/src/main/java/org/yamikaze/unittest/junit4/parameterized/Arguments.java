package org.yamikaze.unittest.junit4.parameterized;

import java.util.List;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 14:57
 */
public class Arguments {

    /**
     * All params in this case.
     */
    private List<Param> params;


    public Arguments() {
    }

    public Arguments(List<Param> params) {
        this.params = params;
    }

    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }
}
