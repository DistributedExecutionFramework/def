package at.enfilo.def.common.api;

import java.util.function.Consumer;

/**
 * Created by mase on 22.08.2016.
 */
@FunctionalInterface
public interface IThrowingConsumer<T> extends Consumer<T> {
    @Override
    default void accept(final T t) {
        try {
            acceptThrows(t);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    void acceptThrows(T t) throws Exception;
}
