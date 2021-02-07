package org.yamikaze.unit.test.spi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-08-14 10:29
 */
public final class ExtensionFactory {

    private static final Map<Class<?>, List<?>> EXTENSION_MAP = new ConcurrentHashMap<>(16);

    private static final Comparator<Object> SORTER = (e1, e2) -> {
        Order order1 = e1.getClass().getAnnotation(Order.class);
        Order order2 = e2.getClass().getAnnotation(Order.class);

        int realOrder1 = order1 == null ? Integer.MAX_VALUE : order1.value();
        int realOrder2 = order2 == null ? Integer.MAX_VALUE : order2.value();


        return Integer.compare(realOrder1, realOrder2);
    };

    @SuppressWarnings("unchecked")
    public static <T> List<T> getExtensions(Class<T> clz) {

        if (clz == null) {
            throw new IllegalStateException("clz is null");
        }

        if (!clz.isInterface()) {
            throw new IllegalStateException("clz " + clz.getName() + " is not an interface");
        }

        List<T> list = (List<T>)EXTENSION_MAP.get(clz);
        if (list != null) {
            return list;
        }

        ServiceLoader<T> serviceLoader = ServiceLoader.load(clz);
        Iterator<T> iterator = serviceLoader.iterator();

        List<T> extensions = new ArrayList<>();
        while (iterator.hasNext()) {
            extensions.add(iterator.next());
        }

        if (!extensions.isEmpty()) {
            extensions.sort(SORTER);
        }

        EXTENSION_MAP.put(clz, extensions);
        return extensions;
    }


    private ExtensionFactory() {

    }
}
