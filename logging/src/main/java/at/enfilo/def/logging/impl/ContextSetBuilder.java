package at.enfilo.def.logging.impl;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.impl.LazyTuple;
import at.enfilo.def.common.impl.Tuple;
import at.enfilo.def.logging.api.ContextIndicator;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created by mase on 22.02.2017.
 */
public class ContextSetBuilder {

    private final Set<ITuple<ContextIndicator, ?>> contextSet;

    public ContextSetBuilder() {
        contextSet = new HashSet<>();
    }

    public ContextSetBuilder add(ContextIndicator contextIndicator, String contextValue) {
        contextSet.add(new Tuple<>(contextIndicator, contextValue));
        return this;
    }

    public ContextSetBuilder add(ContextIndicator contextIndicator, Supplier<?> contextSupplier) {
        contextSet.add(new LazyTuple<>(contextIndicator, contextSupplier));
        return this;
    }

    public Set<ITuple<ContextIndicator, ?>> build() {
        return contextSet;
    }
}
