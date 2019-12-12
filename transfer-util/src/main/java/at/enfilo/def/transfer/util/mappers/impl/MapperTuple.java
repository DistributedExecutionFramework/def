package at.enfilo.def.transfer.util.mappers.impl;

import at.enfilo.def.common.api.ITuple;

/**
 * Created by mase on 26.08.2016.
 */
public class MapperTuple<TSource, TDestination> implements ITuple<Class<TSource>, Class<TDestination>> {

    private final Class<TSource> sourceClass;
    private final Class<TDestination> destinationClass;

    private MapperTuple(Class<TSource> sourceClass, Class<TDestination> destinationClass) {
        this.sourceClass = sourceClass;
        this.destinationClass = destinationClass;
    }

    public static <TSource, TDestination> MapperTuple<TSource, TDestination> wrap(
        Class<TSource> sourceClass,
        Class<TDestination> destinationClass
    ) {
        return new MapperTuple<>(sourceClass, destinationClass);
    }

    @Override
    public Class<TSource> getKey() {
        return sourceClass;
    }

    @Override
    public Class<TDestination> getValue() {
        return destinationClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MapperTuple)) {
            return false;
        }

        MapperTuple<?, ?> that = (MapperTuple<?, ?>) o;

        if (sourceClass != null ? !sourceClass.equals(that.sourceClass) : that.sourceClass != null) return false;
        return destinationClass != null ? destinationClass.equals(that.destinationClass) : that.destinationClass == null;
    }

    @Override
    public int hashCode() {
        int result = sourceClass != null ? sourceClass.hashCode() : 0;
        result = 31 * result + (destinationClass != null ? destinationClass.hashCode() : 0);

        return result;
    }
}
