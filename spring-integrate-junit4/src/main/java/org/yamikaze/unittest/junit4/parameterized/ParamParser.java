package org.yamikaze.unittest.junit4.parameterized;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-17 11:30
 */
public interface ParamParser {

    /**
     * Resolve and parse source params to actual params.
     * @param arguments  source params.
     * @param methodName method name.
     * @param contexts   parameter contexts includes parameter types.
     * @return           actual params array.
     * @throws Throwable if parse occurred error.
     */
    Object[] parse(Arguments arguments, String methodName, ParameterDescriptor...contexts) throws Throwable;

}
