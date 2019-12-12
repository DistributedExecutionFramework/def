package at.enfilo.def.common.api;

import java.util.Map;

/**
 * Created by mase on 25.10.2016.
 */
public interface ITimeoutMap<K, V> extends Map<K, V>, ITouchable<K> {

    /**
     * Checks if map entry is expired.
     *
     * @param key to identify map entry.
     * @return true if is expired, false if not.
     */
    boolean isExpired(K key);
}
