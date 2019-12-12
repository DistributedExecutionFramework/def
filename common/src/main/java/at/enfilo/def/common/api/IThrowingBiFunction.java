package at.enfilo.def.common.api;

import java.util.function.BiFunction;

/**
 * Created by mase on 22.08.2016.
 */
@FunctionalInterface
public interface IThrowingBiFunction<T, V, R> extends BiFunction<T, V, R> {
    @Override
    default R apply(final T t, final V v) {
        try {
            return applyThrows(t, v);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    R applyThrows(T t, V v) throws Exception;
}
