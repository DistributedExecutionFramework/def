package at.enfilo.def.common.impl;

import at.enfilo.def.common.api.ITuple;

import java.util.function.Supplier;

/**
 * Created by mase on 22.02.2017.
 */
public class LazyTuple<K, V> implements ITuple<K, V> {

    private final ITuple<K, Supplier<V>> subjectTuple;
    private final boolean isPersistent;

    private V value;

    public LazyTuple(K key, Supplier<V> valueSupplier) {
        this.subjectTuple = new Tuple<>(key, valueSupplier);
        this.isPersistent = true;
    }

    public LazyTuple(K key, Supplier<V> valueSupplier, boolean isPersistent) {
        this.subjectTuple = new Tuple<>(key, valueSupplier);
        this.isPersistent = isPersistent;
    }

    @Override
    public K getKey() {
        return subjectTuple.getKey();
    }

    @Override
    public V getValue() {
        if (value == null || !isPersistent) {
            value = subjectTuple.getValue().get();
        }
        return value;
    }
}
