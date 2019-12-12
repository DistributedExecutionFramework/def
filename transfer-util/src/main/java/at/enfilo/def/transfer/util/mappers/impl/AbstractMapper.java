package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.common.api.IAssociable;
import at.enfilo.def.transfer.util.mappers.api.IMapperFunction;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by mase on 16.08.2016.
 */
public abstract class AbstractMapper<TSource, TDestination>
implements IMapperFunction<TSource, TDestination>, IAssociable<MapperTuple<TSource, TDestination>> {

    private final MapperTuple<TSource, TDestination> mapperTuple;

    protected AbstractMapper(Class<TSource> sourceClass, Class<TDestination> destinationClass) {
        mapperTuple = MapperTuple.wrap(sourceClass, destinationClass);
    }

    public static <T> void mapAttributes(Supplier<T> supplier, Consumer<T> consumer)
    throws IllegalArgumentException {
        if (supplier != null && consumer != null) {
            T t = supplier.get();

            consumer.accept(t);
        }
        else throw new IllegalArgumentException("Supplier and/or consumer can not be null!");
    }

    public static <T, U> void mapAttributes(Supplier<T> supplier, Consumer<U> consumer, Function<T, U> mapper) {
        mapAttributes(supplier, consumer, mapper, true);
    }

    public static <T, U> void mapAttributes(Supplier<T> supplier, Consumer<U> consumer, Function<T, U> mapper, boolean skipNull)
    throws IllegalArgumentException, IllegalStateException {

        if (supplier != null && consumer != null && mapper != null) {
            T t = supplier.get();

            boolean isNullValue = t == null;

            if (isNullValue && !skipNull) throw new IllegalStateException("Supplied value is null while skipNull argument is false!");
            else if (!isNullValue) {
                U u = mapper.apply(t);
                consumer.accept(u);
            }
        }
        else throw new IllegalArgumentException("Supplier and/or consumer and/or mapper can not be null!");
    }

    @Override
    public MapperTuple<TSource, TDestination> getAssociation() {
        return mapperTuple;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractMapper)) return false;

        AbstractMapper<?, ?> that = (AbstractMapper<?, ?>) o;
        return mapperTuple != null ? mapperTuple.equals(that.mapperTuple) : that.mapperTuple == null;
    }

    @Override
    public int hashCode() {
        return mapperTuple.hashCode();
    }
}
