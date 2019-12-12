package at.enfilo.def.transfer.util.mappers.api;

/**
 * Created by mase on 26.08.2016.
 */
public interface IMapperFunction<T, R> {
    R map(T from, R instance) throws IllegalArgumentException, IllegalStateException;
}
