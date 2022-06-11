package org.yamikaze.unit.test.mock;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-05-14 20:38
 */
public class TestContext {

    private static final ThreadLocal<TestInfo> TEST_INFO = new ThreadLocal<>();

    public static String getCurrentTestClass() {
        TestInfo testInfo = TEST_INFO.get();
        if (testInfo != null) {
            return testInfo.getTestClassName();
        }

        return null;
    }

    public static String getCurrentTestMethod() {
        TestInfo testInfo = TEST_INFO.get();
        if (testInfo != null) {
            return testInfo.getTestMethodName();
        }

        return null;
    }

    public static void setTestInfo(String className, String methodName) {
        TEST_INFO.set(new TestInfo(className, methodName));
    }


}

class TestInfo {

    private String testClassName;

    private String testMethodName;

    TestInfo(String testClassName, String testMethodName) {
        this.testClassName = testClassName;
        this.testMethodName = testMethodName;
    }

    String getTestClassName() {
        return testClassName;
    }

    public void setTestClassName(String testClassName) {
        this.testClassName = testClassName;
    }

    String getTestMethodName() {
        return testMethodName;
    }

    public void setTestMethodName(String testMethodName) {
        this.testMethodName = testMethodName;
    }
}
