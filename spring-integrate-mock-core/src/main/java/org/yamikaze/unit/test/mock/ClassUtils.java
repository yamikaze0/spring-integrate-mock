package org.yamikaze.unit.test.mock;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.AntPathMatcher;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author qinluo
 * @version 1.0.0
 * @since 2019-04-09 10:00
 */
public class ClassUtils {

    public static List<Class<?>> getAllSuperClass(Class<?> clz) {
        if (clz == null || clz == Object.class) {
            return new ArrayList<>(0);
        }


        List<Class<?>> supers = new ArrayList<>(16);

        Class<?> internal = clz.getSuperclass();

        while (internal != Object.class && internal != null) {
            supers.add(internal);
            internal = internal.getSuperclass();
        }

        return supers;
    }

    public static List<Method> getCandidateMethods(Class<?> clz, String methodPattern) {
        if (methodPattern == null || clz == null) {
            return new ArrayList<>(1);
        }

        Method[] declaredMethods = clz.getDeclaredMethods();
        if (declaredMethods.length == 0) {
            return new ArrayList<>(1);
        }

        List<Method> candidateMethods = new ArrayList<>(16);
        AntPathMatcher antPathMatcher = new AntPathMatcher();

        for (Method method : declaredMethods) {
            int modifiers = method.getModifiers();
            boolean candidateMethod = (Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers))
                    && (methodPattern.equals(method.getName()) || antPathMatcher.match(methodPattern, method.getName()));
            if (candidateMethod) {
                candidateMethods.add(method);
            }
        }

        return candidateMethods;
    }

    public static String appendClasses(Class<?>[] types, boolean simple) {
        if (types == null || types.length == 0) {
            return "()";
        }

        StringBuilder sb = new StringBuilder(256);
        sb.append("(");
        int index = 0;
        for (Class<?> type : types) {
            if (simple) {
                sb.append(type.getSimpleName());
            } else {
                sb.append(type.getTypeName());
            }

            if (index++ != types.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }


    public static Method findMethod(Class<?> clz, String methodName, Class<?> ...parameterClz) {
        if (clz == null || methodName == null) {
            throw new IllegalArgumentException("parameter [clz] or [methodName] must not be null!");
        }

        Method declaredMethod = null;

        try {
            declaredMethod = clz.getDeclaredMethod(methodName, parameterClz);
        } catch (NoSuchMethodException e) {
            // do nothing
        }

        return declaredMethod;
    }

    public static List<Class<?>> getAllSuperClassesAndInterfaces(Class<?> clz) {
        Set<Class<?>> allClasses = new HashSet<>(64);
        allClasses.add(clz.getSuperclass());

        Class<?>[] interfaces = clz.getInterfaces();
        if (interfaces.length == 0) {
            allClasses.remove(Object.class);
            return new ArrayList<>(allClasses);
        }

        allClasses.addAll(getAllSuperClassesAndInterfaces(clz.getSuperclass()));

        Collections.addAll(allClasses, interfaces);

        for (Class<?> parentInterface : interfaces) {
            allClasses.add(parentInterface);
            allClasses.addAll(getAllSuperClassesAndInterfaces(parentInterface));
        }

        allClasses.remove(null);
        allClasses.remove(Object.class);

        return new ArrayList<>(allClasses);
    }

    public static Class<?> extractClosedDeclaringClass(Class<?> declaringClass, Method method) {
        if (!isProxyClass(declaringClass)) {
            return declaringClass;
        }

        List<Class<?>> allClassList = getAllSuperClassesAndInterfaces(declaringClass);
        if (allClassList.size() == 0) {
            return declaringClass;
        }

        Class<?> candidateClass = null;

        for (Class<?> clz : allClassList) {
            if (clz == Object.class || isProxyClass(clz)) {
                continue;
            }

            Method pmethod = findMethod(clz, method.getName(), method.getParameterTypes());
            if (pmethod == null) {
                continue;
            }

            if (candidateClass == null) {
                candidateClass = clz;
            } else if (candidateClass.isAssignableFrom(clz)) {
                candidateClass = clz;
            }
        }

        if (candidateClass == null) {
            candidateClass = declaringClass;
        }

        return candidateClass;

    }

    public static Class<?> extractClosedUnProxyClass(MethodInvocation invocation) {
        Object target = invocation.getThis();
        if (target == null) {
            return null;
        }

        Class<?> originType = target.getClass();
        Class<?> targetType = originType;
        while (targetType != Object.class) {
            if (isProxyClass(targetType)) {
                targetType = targetType.getSuperclass();
                continue;
            }
            break;
        }

        if (targetType == Object.class || targetType == Proxy.class) {
            Class<?>[] interfaces = originType.getInterfaces();
            if (interfaces.length > 0) {
                return interfaces[0];
            }
        }

        return targetType;
    }

    private static boolean isProxyClass(Class<?> clz) {
        return Proxy.isProxyClass(clz) || org.springframework.util.ClassUtils.isCglibProxyClass(clz)
                || clz.getName().contains("bytecode")
                || clz.getName().contains("CGLIB");
    }

    public static boolean compareMethod(Method method0, Method method) {
        if (!method.getName().equals(method0.getName())) {
            return false;
        }

        if (method0.getParameterCount() != method.getParameterCount()) {
            return false;
        }

        if (method0.isVarArgs() != method.isVarArgs()) {
            return false;
        }

        Class<?>[] parameterTypes0 = method0.getParameterTypes();
        Class<?>[] parameterTypes = method.getParameterTypes();


        for (int i = 0; i < method0.getParameterCount(); i++) {
            if (parameterTypes[i] != parameterTypes0[i]) {
                return false;
            }
        }

        return true;
    }

    public static Class<?> initialization(String classname, ClassLoader cl) {
        try {
            return Class.forName(classname, true, cl);
        } catch (Exception e) {
            return null;
        }
    }

    public static Object invoke(Method m, Object obj, Object...args) {
        try {
            return m.invoke(obj, args);
        } catch (Exception e) {
            return null;
        }
    }
}
