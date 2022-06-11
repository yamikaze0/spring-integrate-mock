package org.yamikaze.unit.test.mock.proxy;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2019-11-12 10:22
 */
public class ProxyWrapper {

    private Class<?> mockType;

    private Object mockObject;

    public ProxyWrapper(Class<?> mockType, Object mockObject) {
        this.mockType = mockType;
        this.mockObject = mockObject;
    }

    public Class<?> getMockType() {
        return mockType;
    }

    public void setMockType(Class<?> mockType) {
        this.mockType = mockType;
    }

    public Object getMockObject() {
        return mockObject;
    }

    public void setMockObject(Object mockObject) {
        this.mockObject = mockObject;
    }

    @Override
    public int hashCode() {
        return mockType.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProxyWrapper) {
            return mockType.equals(((ProxyWrapper) obj).mockType);
        }

        return false;
    }
}
