package at.enfilo.def.common.api;

/**
 * Created by mase on 27.10.16.
 */
public interface ITouchable<K> {

    /**
     * Touches specified key.
     *
     * @param key key to touch.
     * @return true if was touched, false if not.
     */
    boolean touch(K key);
}
