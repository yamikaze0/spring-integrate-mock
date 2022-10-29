package org.yamikaze.unit.test.mock;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yamikaze.unit.test.check.MethodDescriptor;
import org.yamikaze.unit.test.handler.HandlerSupport;
import org.yamikaze.unit.test.mock.answer.AbstractAnswer;
import org.yamikaze.unit.test.mock.answer.Answer;
import org.yamikaze.unit.test.mock.argument.ArgumentMatcher;
import org.yamikaze.unit.test.mock.proxy.MockInvocation;
import org.yamikaze.unit.test.spi.JsonObjectMapperProxy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-17 11:18
 * cs:off
 */
public class MockRecordHandler extends HandlerSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockRecordHandler.class);

    private static final Comparator<Answer> COMPARATOR = (o1, o2) -> {
        if (!(o1 instanceof OrderedAnswer) || !(o2 instanceof OrderedAnswer)) {
            return 0;
        }

        int order1 = ((OrderedAnswer) o1).order;
        int order2 = ((OrderedAnswer) o2).order;

        return Integer.compare(order1, order2);
    };
    private static final Map<String, Class<?>> PRIMITIVE_TYPES = new HashMap<>(16);

    static {
        PRIMITIVE_TYPES.put("boolean", boolean.class);
        PRIMITIVE_TYPES.put("int", int.class);
        PRIMITIVE_TYPES.put("byte", byte.class);
        PRIMITIVE_TYPES.put("char", char.class);
        PRIMITIVE_TYPES.put("short", short.class);
        PRIMITIVE_TYPES.put("double", double.class);
        PRIMITIVE_TYPES.put("float", float.class);
        PRIMITIVE_TYPES.put("long", long.class);
    }

    @Override
    public void before(MethodDescriptor descriptor) {
        parseMockScene(descriptor);
        Mockit mockit = Mockit.MOCKIT;
        //first use current method, second use global config.
        boolean matchParams = mockit.getMatchParams() == null ?
                (Mockit.getGlobalMatchParams() == null || Mockit.getGlobalMatchParams()) : mockit.getMatchParams();
        String dir = mockit.getMockDataDir();
        File dirFile = new File(mockit.getAbsDir() + GlobalConfig.getSaveResourceLocation(), dir);

        //There has no Local file data.
        if (!dirFile.isDirectory()) {
            return;
        }

        File[] subFiles = dirFile.listFiles();
        if (subFiles == null || subFiles.length == 0) {
            LOGGER.info("file {} is empty. skip load local file data", dirFile.getAbsolutePath());
            return;
        }

        Map<RecordBehaviorWrapper, RecordBehavior> recordBehaviorMap = new HashMap<>(64);
        //Key format: full ClassName-methodName(paramTypes)invokeTimes.json
        for (File file : subFiles) {
            if (!isValidFile(file)) {
                LOGGER.warn("file {} is invalid file", file.getAbsolutePath());
                continue;
            }

            //Suffix
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            String key = name.substring(0, name.length() - 1);
            int paramTypesStartIndex = name.indexOf("(");
            int paramTypesEndIndex = name.indexOf(")");
            int classEndIndex = name.indexOf("-");
            int times = Integer.parseInt(name.substring(paramTypesEndIndex + 1));

            String className = name.substring(0, classEndIndex);
            String methodName = name.substring(classEndIndex + 1, paramTypesStartIndex);
            String paramTypes = name.substring(paramTypesStartIndex + 1, paramTypesEndIndex);

            String[] split = paramTypes.trim().length() == 0 ? new String[0] : paramTypes.split("[,]+");
            Class<?>[] params = new Class[split.length];

            boolean occurredUnknownClz = false;
            int i = 0;
            for (String clz : split) {
                Class<?> paramClz = forName(clz.trim());
                if (paramClz == null) {
                    occurredUnknownClz = true;
                    LOGGER.error("parse mock file occurred unknown clz {}", clz);
                    break;
                }

                params[i++] = paramClz;
            }

            Class<?> targetClass = forName(className);
            if (targetClass == null) {
                LOGGER.error("parse mock file occurred unknown clz {}", className);
                occurredUnknownClz = true;
            }

            if (occurredUnknownClz) {
                continue;
            }

            Method method = ClassUtils.findMethod(targetClass, methodName, params);
            if (method == null) {
                LOGGER.warn("class " + targetClass.getName() + " not found method " + methodName + ClassUtils.appendClasses(params, true));
                continue;
            }


            MockData mockData = parseMockData(file);
            if (mockData == null) {
                LOGGER.warn("file " + file.getName() + " is empty file ");
                continue;
            }
            LocalFileDataAnswer localFileDataAnswer = new LocalFileDataAnswer(file);
            localFileDataAnswer.setMockData(mockData);
            List<ArgumentMatcher> argumentMatchers = new ArrayList<>(0);
            matchParams = matchParams && (mockData.getMatchParam() == null || mockData.getMatchParam());
            if (matchParams) {
                argumentMatchers = parseArgumentMatchers(mockData, method);
            }

            RecordBehaviorWrapper wrapper = new RecordBehaviorWrapper(key, argumentMatchers);
            RecordBehavior recordBehavior = recordBehaviorMap.get(wrapper);
            if (recordBehavior == null) {
                recordBehavior = new MockitoRecordBehavior();
                recordBehaviorMap.put(wrapper, recordBehavior);
                recordBehavior.setClz(targetClass);
                recordBehavior.setMethod(method);

                if (argumentMatchers.size() > 0) {
                    recordBehavior.setMatchParams(true);
                    for (ArgumentMatcher argumentMatcher : argumentMatchers) {
                        recordBehavior.addArgumentMatcher(argumentMatcher);
                    }
                }
                RecordBehaviorList.INSTANCE.addRecordBehavior(recordBehavior);
            }

            Answer answer = new OrderedAnswer(times, localFileDataAnswer);
            recordBehavior.addAnswer(answer);
        }

        recordBehaviorMap.forEach((k, v) -> v.getAnswers().sort(COMPARATOR));
    }

    private List<ArgumentMatcher> parseArgumentMatchers(MockData mockData, Method method) {
        List<ArgumentMatcher> argumentMatchers = new ArrayList<>();

        //如果方法没有入参或者没有采集到入参 则不进行参数匹配
        List<String> params = mockData.getParams();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length <= 0 || params == null || params.size() <= 0) {
            return argumentMatchers;
        }

        if (parameterTypes.length != params.size()) {
            LOGGER.error("Mock数据采集，采集到的方法参数与现有方法参数不一致，method = {}, class = {}",
                    method.getName(), method.getDeclaringClass().getName());
            return argumentMatchers;
        }

        int paramIndex = 0;

        Type[] genericParameterTypes = method.getGenericParameterTypes();
        try {
            for (Class<?> parameterType : parameterTypes) {
                String paramJson = params.get(paramIndex);

                Type type = parameterType;
                Type genericParamType = genericParameterTypes[paramIndex];
                if (genericParamType instanceof ParameterizedType) {
                    type = genericParamType;
                }

                Object paramObject = JsonObjectMapperProxy.decode(paramJson, type);
                argumentMatchers.add(new MockRecordArgumentMatcher(paramObject, method, paramIndex++));
            }
        } catch (Exception e) {
            LOGGER.error("parse argument matcher error, method = {}{}", method.getName(), ClassUtils.appendClasses(method.getParameterTypes(), true));
            LOGGER.error("error reason is,", e);

            argumentMatchers.clear();
        }

        return argumentMatchers;
    }

    private static Class<?> forName(String className) {
        if (PRIMITIVE_TYPES.containsKey(className)) {
            return PRIMITIVE_TYPES.get(className);
        }

        String originClassName = className;
        int arrayIndex = className.indexOf('[');
        boolean isArrayClass = false;
        if (arrayIndex > 0) {
            isArrayClass = true;
            originClassName = originClassName.substring(0, arrayIndex);
        }

        int arrayDimension = 0;
        if (isArrayClass) {
            arrayDimension = (className.length() - originClassName.length()) / 2;
        }

        Class<?> clz;
        try  {
            if (PRIMITIVE_TYPES.containsKey(originClassName)) {
                clz = PRIMITIVE_TYPES.get(originClassName);
            } else {
                clz = Class.forName(originClassName);
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error("unknown class {}", originClassName);
            return null;
        }

        if (isArrayClass) {
            int[] dimensions = new int[arrayDimension];
            Arrays.fill(dimensions, 1);

            Object obj = Array.newInstance(clz, dimensions);
            clz = obj.getClass();
        }

        return clz;
    }

    private static final Pattern METHOD_PATTERN = Pattern.compile("^[_a-zA-z][\\da-zA-Z_]*");

    private boolean isValidFile(File file) {
        if (!file.exists()) {
            LOGGER.error("file {} not exist", file.getName());
            return false;
        }

        if (!file.isFile()) {
            LOGGER.error("file {} not file", file.getName());
            return false;
        }

        String name = file.getName();
        if (!name.endsWith(".json")) {
            LOGGER.error("file {} is not json file", file.getName());
            return false;
        }

        String[] split = name.split("-");
        if (split.length != 2) {
            LOGGER.error("filename  {} must be className-methodName(parameterTypes)invokeTimes.json", file.getName());
            return false;
        }

        String className = split[0];
        if (StringUtils.isBlank(className)) {
            LOGGER.error("filename {} must be className-methodName(parameterTypes)invokeTimes.json", file.getName());
            return false;
        }

        int paramStartIndex = name.indexOf("(");
        int paramEndIndex = name.indexOf(")");
        if (paramStartIndex == -1 || paramStartIndex < className.length() + 2) {
            LOGGER.error("filename {} must be className-methodName(parameterTypes)invokeTimes.json", file.getName());
            return false;
        }

        if (paramEndIndex <= paramStartIndex) {
            LOGGER.error("filename {} must be className-methodName(parameterTypes)invokeTimes.json", file.getName());
            return false;
        }

        name = name.substring(0, file.getName().lastIndexOf("."));
        String invokeTimes = name.substring(paramEndIndex + 1);
        if (StringUtils.isBlank(invokeTimes)) {
            LOGGER.error("filename {} invokeTimes can't be blank", file.getName());
            return false;
        }

        String methodName = split[1].substring(0, paramStartIndex - className.length() - 1);
        if (StringUtils.isBlank(methodName)) {
            LOGGER.error("filename {} methodName can't be blank", file.getName());
            return false;
        }

        if (!METHOD_PATTERN.matcher(methodName).matches()) {
            LOGGER.error("filename {} methodName {} is invalid", file.getName(), methodName);
            return false;
        }

        return true;
    }

    public static class OrderedAnswer extends AbstractAnswer {
        private final int order;
        private final LocalFileDataAnswer answer;

        OrderedAnswer(int order, LocalFileDataAnswer answer) {
            this.order = order;
            this.answer = answer;
        }

        public LocalFileDataAnswer getAnswer() {
            return answer;
        }

        @Override
        public boolean accessed() {
            return answer.accessed();
        }

        @Override
        public Object answer(MockInvocation invocation) {
            return this.answer.answer(invocation);
        }

        @Override
        public String toString() {
            return this.answer.toString();
        }
    }

    private MockData parseMockData(File file) {
        InputStream inputStream = null;

        if (!file.isFile()) {
            throw new IllegalStateException("file " + file.getAbsolutePath() + " is not file.");
        }

        try {
            inputStream = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            byte[] cache = new byte[1024];
            int i;
            while ((i = inputStream.read(cache)) != -1) {
                baos.write(cache, 0, i);
            }

            return JsonObjectMapperProxy.decode(baos.toString(), new TypeToken<MockData>() {}.getType());
        } catch (IOException e) {
            throw new MockException("load mock file " + file.getAbsolutePath() + " fail.", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Override
    public void after(MethodDescriptor descriptor) {
        LocalFileDataWriter.getWriter().write();
        LocalFileDataWriter.getWriter().clearTask();

        Mockit.clear();
    }

    private void parseMockScene(MethodDescriptor descriptor) {
        //pre clear
        Mockit.clear();

        String testClassName = descriptor.getClassName();
        String testMethodName = descriptor.getMethodName();
        Mockit contextConfig = Mockit.MOCKIT;
        if (StringUtils.isBlank(contextConfig.getSceneCode())) {
            contextConfig.setSceneCode(testMethodName);
        }
        contextConfig.setClassName(testClassName);
        contextConfig.setAbsDir(System.getProperty("user.dir"));
    }
}
