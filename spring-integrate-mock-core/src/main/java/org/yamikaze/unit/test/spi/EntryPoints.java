package org.yamikaze.unit.test.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qinluo
 * @date 2022-10-19 22:08:19
 * @since 1.0.0
 */
public class EntryPoints {

    private static final Map<Integer, List<EntryPoint>> ENTRY = new ConcurrentHashMap<>();

    static {
        List<EntryPoint> extensions = ExtensionFactory.getExtensions(EntryPoint.class);
        extensions.forEach(EntryPoint::register);
    }

    public static void register(int code, EntryPoint point) {
        List<EntryPoint> entries = ENTRY.getOrDefault(code, new ArrayList<>());
        entries.add(point);
        ENTRY.put(code, entries);
    }

    public static void execute(int code, Object[] args) {
        List<EntryPoint> entries = ENTRY.get(code);
        if (entries == null || entries.size() == 0) {
            return;
        }

        entries.forEach(p -> p.execute(code, args));
    }
}
