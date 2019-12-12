package at.enfilo.def.security.annotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by mase on 31.10.16.
 */
public class SecuredAnnotationProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecuredAnnotationProcessor.class);

    private SecuredAnnotationProcessor() {
        // Hiding public constructor
    }

    public static boolean isAnnotated(AnnotatedElement annotatedElement) {
        if (annotatedElement != null) {

            if (annotatedElement.isAnnotationPresent(Secured.class)) {
                return true;
            }
            else if (annotatedElement instanceof Class) {
                for (AnnotatedType annotatedType : ((Class) annotatedElement).getAnnotatedInterfaces()) {
                    if (annotatedType.isAnnotationPresent(Secured.class)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isAnnotated(
        Class<?> annotatedClass,
        Method annotatedMethod
    ) {

        if (isAnnotated(annotatedMethod)) {
            return true;
        }
        else if (annotatedClass != null && annotatedMethod != null) {

            // Will look for annotations in generic interfaces.
            Type[] genericInterfaces = annotatedClass.getGenericInterfaces();

            // Checking out generic interfaces annotations.
            for (Type type : genericInterfaces) {
                if (type instanceof Class) try {

                    // In this case we have to "search" for method in interface.
                    // However we know how implemented method looks like - has same name and method
                    // signature as resourceMethod.
                    Method genericMethod = ((Class<?>) type).getMethod(
                        annotatedMethod.getName(),
                        annotatedMethod.getParameterTypes()
                    );

                    return isAnnotated(genericMethod);

                } catch (NoSuchMethodException e) {
                    LOGGER.trace("Method not found.", e);
                }
            }
        }
        return false;
    }

    public static Set<Role> extractRoles(
        Class<?> annotatedClass,
        Map<AnnotatedElement, Set<Role>> rolesCache
    ) {

        if (annotatedClass == null) {
            return Collections.emptySet();
        }

        // Get the called resource class to extract roles specified in Secured annotation.
        Set<Role> classRoles = getFromCacheOrExtract(annotatedClass, rolesCache);

        if (classRoles.isEmpty()) {

            // If class roles are empty we will look for annotations in generic interfaces.
            Type[] genericInterfaces = annotatedClass.getGenericInterfaces();
            classRoles = new HashSet<>();

            // Checking out generic interfaces annotations for class roles.
            for (Type type : genericInterfaces) {
                Set<Role> extractedRoles = extractRoles(type.getClass());
                if (!extractedRoles.isEmpty()) {
                    classRoles.addAll(extractedRoles);
                }
            }
        }

        if (rolesCache != null) {
            rolesCache.put(annotatedClass, classRoles);
        }
        return classRoles;
    }

    public static Set<Role> extractRoles(
        Class<?> annotatedClass,
        Method annotatedMethod
    ) {
        return extractRoles(annotatedClass, annotatedMethod, null);
    }

    public static Set<Role> extractRoles(
        Class<?> annotatedClass,
        Method annotatedMethod,
        Map<AnnotatedElement, Set<Role>> rolesCache
    ) {

        if (annotatedClass == null || annotatedMethod == null) {
            return Collections.emptySet();
        }

        // Get the called resource method to extract roles specified in Secured annotation.
        Set<Role> methodRoles = getFromCacheOrExtract(annotatedMethod, rolesCache);

        if (methodRoles.isEmpty()) {

            // If method roles are empty we will look for annotations in generic interfaces.
            Type[] genericInterfaces = annotatedClass.getGenericInterfaces();
            methodRoles = new HashSet<>();

            // Checking out generic interfaces annotations for method roles.
            for (Type type : genericInterfaces) {
                if (type instanceof Class) try {

                    // In this case we have to "search" for method in interface.
                    // However we know how implemented method looks like - has same name and method
                    // signature as resourceMethod.
                    Method genericMethod = ((Class<?>) type).getMethod(
                        annotatedMethod.getName(),
                        annotatedMethod.getParameterTypes()
                    );

                    Set<Role> extractedRoles = extractRoles(genericMethod);
                    if (!extractedRoles.isEmpty()) methodRoles.addAll(extractedRoles);

                } catch (NoSuchMethodException e) {
                    LOGGER.trace("Method not found.", e);
                }
            }
        }

        if (rolesCache != null) {
            rolesCache.put(annotatedMethod, methodRoles);
        }
        return methodRoles;
    }

    public static Set<Role> extractRoles(AnnotatedElement annotatedElement) {
        // Extract the roles from the annotated element.
        if (isAnnotated(annotatedElement)) {
            Role[] roles = annotatedElement.getAnnotation(Secured.class).value();
            return Arrays.stream(roles).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    private static Set<Role> getFromCacheOrExtract(
        AnnotatedElement annotatedElement,
        Map<AnnotatedElement, Set<Role>> rolesCache
    ) {
        if (rolesCache != null && rolesCache.containsKey(annotatedElement)) {
            return rolesCache.get(annotatedElement);
        }
        return extractRoles(annotatedElement);
    }
}
