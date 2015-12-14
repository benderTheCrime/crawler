package core.util;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by josephgroseclose on 12/13/15.
 */
public class MapUtil {
    public static <K, V extends Comparable<? super V>> Map<K, V>
            sortByValue(Map<K, V> map)
    {
        Map<K,V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K,V>> stream = map.entrySet().stream();

        stream.sorted(Comparator.comparing(e -> e.getValue()))
                .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }
}
