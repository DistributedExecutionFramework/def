package at.enfilo.def.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectionUtils {

    public static <K, V> void putOrAdd(Map<K, List<V>> map, K key, V value) {
        if (map.containsKey(key)) {
            if (map.get(key) == null) {
                List<V> list = new ArrayList<>();
                list.add(value);
                map.put(key, list);
            } else {
                map.get(key).add(value);
            }
        } else {
            List<V> list = new ArrayList<>();
            list.add(value);
            map.put(key, list);
        }
    }
}
