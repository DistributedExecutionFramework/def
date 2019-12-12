package at.enfilo.def.common.impl;

import at.enfilo.def.common.api.ITuple;

/**
 * Created by mase on 22.02.2017.
 */
public class Tuple<K, V> implements ITuple<K, V> {

    private final K key;
    private final V value;

    public Tuple(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }
}
