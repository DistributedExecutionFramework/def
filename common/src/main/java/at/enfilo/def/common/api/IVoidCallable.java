package at.enfilo.def.common.api;

import java.util.concurrent.Callable;

/**
 * Created by mase on 16.09.2016.
 */
@FunctionalInterface
public interface IVoidCallable {
    void call() throws Exception;

    /**
     * Convenience method that converts {@code IVoidCallable} to {@code Callable<Void>}.
     *
     * @return {@code Callable<Void>} instance.
     */
    default Callable<Void> toCallable() {
        return () -> {
            this.call();
            return null;
        };
    }
}
