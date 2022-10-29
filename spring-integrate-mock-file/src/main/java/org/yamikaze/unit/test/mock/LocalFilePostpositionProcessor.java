package org.yamikaze.unit.test.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.yamikaze.unit.test.spi.JsonObjectMapperProxy;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-17 10:01
 */
public class LocalFilePostpositionProcessor implements PostpositionProcessor {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFilePostpositionProcessor.class);

    private static final LocalFileDataWriter WRITER = LocalFileDataWriter.getWriter();

    @Override
    public void afterRealInvokeProcess(InternalMethodInvocation mit, Object result, Object... args) {
        if (!matchMockCondition(mit)) {
            return;
        }

        boolean mock = Mockit.MOCKIT.isMock();
        if (mock) {
            LOGGER.error("no mock invoke. {}", mit);
        }

        storeMockResult(mit, result, args);

    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LocalFilePostpositionProcessor);

    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private boolean matchMockCondition(InternalMethodInvocation mit) {
        if (Mockit.MOCKIT.getInterfaceAndMethods() == null || !Mockit.MOCKIT.isEnabled()) {
            return false;
        }
        Class<?> declaringClass = mit.getDeclaringClass();
        Method method = mit.getMethod();
        String str = declaringClass.getName() + "-" + method.getName();
        for (String pattern : Mockit.MOCKIT.getInterfaceAndMethods()) {
            if (Objects.equals(pattern, str) || PATH_MATCHER.match(pattern, str)) {
                return true;
            }
        }
        return false;
    }

    private void storeMockResult(InternalMethodInvocation mit, Object result, Object... args) {
        String dir = Mockit.MOCKIT.getMockDataDir();
        File dirFile = new File(Mockit.MOCKIT.getAbsDir() + GlobalConfig.getSaveResourceLocation(), dir);
        if (!dirFile.isDirectory()) {
            if (!dirFile.mkdirs()) {
                LOGGER.error("create directory {} fail. store mock result skip.", dirFile.getAbsolutePath());
            }
        }

        String paramTypes = ClassUtils.appendClasses(mit.getMethod().getParameterTypes(), false);
        String key = mit.getDeclaringClass().getName() + "-" + mit.getMethod().getName() + paramTypes;
        int invokeTimes = calculateInvokeTimes(mit);

        //compatible old logic
        invokeTimes = invokeTimes - 1;

        MockData mockData = new MockData();
        mockData.setParams(generateArgs(args));
        mockData.setResult(JsonObjectMapperProxy.encode(result));
        WRITER.addTask(dirFile, key, invokeTimes, mockData);
    }

    private int calculateInvokeTimes(InternalMethodInvocation mit) {
        /*
         * 由于受到参数匹配的影响，此时再去进行从RecordBehavior中寻找Mock的调用次数就比较麻烦了
         * 这儿直接用这个方法的调用次数(真实调用和Mock调用)作为文件的调用次数即可
         *
         * 虽然到时候可能采集到的文件是5 7，不过这样也不影响
         */
        return mit.getRealInvokedTimes() + mit.getMockInvokedTimes();
    }

    private List<String> generateArgs(Object ...args) {
        if (args == null || args.length == 0) {
            return new ArrayList<>(0);
        }

        List<String> argList = new ArrayList<>(args.length);
        for (Object arg : args) {
            argList.add(JsonObjectMapperProxy.encode(arg));
        }

        return argList;
    }

}
