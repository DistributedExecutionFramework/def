package at.enfilo.def.common.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mase on 07.07.2017.
 */
public abstract class AbstractBaseRegistry<K, V> {

    private final Map<K, V> baseRegistryMap;

    /**
     * Hide constructor, use static methods.
     */
    protected AbstractBaseRegistry() {
        this.baseRegistryMap = new HashMap<>();

        // Init registry.
        this.refresh();
    }

    /**
     * Refreshes its state.
     */
    public abstract void refresh();

    /**
     * Returns value associated with a given key.
     *
     * @param key key to be checked.
     * @return value if present, null if not.
     */
    public V get(K key) {
        return baseRegistryMap.get(key);
    }

    /**
     * Updates association with a new value and returns previous value associated with a given key.
     *
     * @param key key to be checked.
     * @param value value to be associated.
     * @return previously associated value.
     */
    public V put(K key, V value) {
        return baseRegistryMap.put(key, value);
    }

    /**
     * Checks if registry contains given key.
     *
     * @param key key to be checked.
     * @return true if contains, false if not.
     */
    public boolean containsKey(K key) {
        return baseRegistryMap.containsKey(key);
    }

    /**
     * Returns all registered data-types as collection.
     * @return all registered data-types as collection.
     */
    public Collection<V> getAll() {
        return baseRegistryMap.values();
    }
}
