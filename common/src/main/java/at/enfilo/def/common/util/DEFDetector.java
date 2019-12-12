package at.enfilo.def.common.util;

import at.enfilo.def.common.api.IThrowingConsumer;
import org.reflections.Reflections;

import java.util.Set;

/**
 * Created by mase on 08.06.2017.
 */
public class DEFDetector {

    private DEFDetector() {
    }

    public static Set<Class<?>> getPackageMemberSet(String packageToScan) {
        // Return a set of all classes found under a package.
        return getSubTypeSet(Object.class, packageToScan);
    }

    public static <T> Set<Class<? extends T>> getSubTypeSet(Class<T> baseType) {
        // Specifying package for search.
        return getSubTypeSet(baseType, baseType.getPackage().getName());
    }

    public static <T> Set<Class<? extends T>> getSubTypeSet(Class<T> baseType, String packageToScan) {
        // Returning subtype set.
        return new Reflections(packageToScan).getSubTypesOf(baseType);
    }

    public static void handlePackageMembers(
        String packageToScan,
        IThrowingConsumer<Class<?>> consumer
    ) {
        Set<Class<?>> memberSet = getPackageMemberSet(packageToScan);
        handleTypes(memberSet, consumer);
    }

    public static <T> void handleSubTypes(
        Class<T> baseType,
        IThrowingConsumer<Class<? extends T>> consumer
    ) {
        Set<Class<? extends T>> subTypeSet = getSubTypeSet(baseType);
        handleTypes(subTypeSet, consumer);
    }

    public static <T> void handleSubTypes(
        Class<T> baseType,
        String packageToScan,
        IThrowingConsumer<Class<? extends T>> consumer
    ) {
        Set<Class<? extends T>> subTypeSet = getSubTypeSet(baseType, packageToScan);
        handleTypes(subTypeSet, consumer);
    }

    private static <T> void handleTypes(
        Set<Class<? extends T>> typeSet,
        IThrowingConsumer<Class<? extends T>> consumer
    ) {
        // Handling automatically discovered classes.
        typeSet.forEach(consumer);
    }
}
