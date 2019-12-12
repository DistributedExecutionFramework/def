package at.enfilo.def.common.api;

/**
 * Created by mase on 22.08.2016.
 */
@FunctionalInterface
public interface IThrowingFunction<T, R> {
    R apply(T t) throws Exception;
}
