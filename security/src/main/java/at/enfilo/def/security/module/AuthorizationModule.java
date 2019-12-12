package at.enfilo.def.security.module;

import at.enfilo.def.security.annotations.Role;
import at.enfilo.def.security.annotations.SecuredAnnotationProcessor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by mase on 31.10.16.
 */
public class AuthorizationModule {

    private AuthorizationModule() {
        // Hiding public constructor
    }

    /**
     * Checks if userId is authorized to execute code stored under specific method or class.
     *
     * @param userId userId to be checked.
     * @param annotatedClass class to be checked (contains annotatedMethod).
     * @param annotatedMethod method to be checked.
     * @return true if authorized, false if not.
     */
    public static boolean isAuthorized(
        String userId,
        Class<?> annotatedClass,
        Method annotatedMethod
    ) {
        return isAuthorized(userId, annotatedClass, annotatedMethod, Collections.emptyMap());
    }

    /**
     * Checks if userId is authorized to execute code stored under specific method or class.
     *
     * @param userId userId to be checked.
     * @param annotatedClass class to be checked (contains annotatedMethod).
     * @param annotatedMethod method to be checked.
     * @param rolesCache store map for [methods / classes] -> [roles].
     * @return true if authorized, false if not.
     */
    public static boolean isAuthorized(
        String userId,
        Class<?> annotatedClass,
        Method annotatedMethod,
        Map<AnnotatedElement, Set<Role>> rolesCache
    ) {

        Set<Role> methodRoles = SecuredAnnotationProcessor.extractRoles(annotatedClass, annotatedMethod, rolesCache);

        // TODO check if the user has one of the allowed roles.

        // Check if the user is allowed to execute the method.
        if (methodRoles.isEmpty()) {

            // We don't have method roles, use global class ones.
            Set<Role> classRoles = SecuredAnnotationProcessor.extractRoles(annotatedClass, rolesCache);

            // checkPermissions(classRoles);
            return true;

        } else {

            // Using explicitly specified method roles.
            // checkPermissions(methodRoles);
            return true;
        }
    }
}
