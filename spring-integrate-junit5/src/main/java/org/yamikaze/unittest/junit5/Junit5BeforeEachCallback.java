package org.yamikaze.unittest.junit5;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @author qinluo
 * @date 2022-06-11 22:58:50
 * @since 1.0.0
 */
public class Junit5BeforeEachCallback implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {

    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {

    }
}
