package at.enfilo.def.security.filter;

import at.enfilo.def.security.annotations.Role;
import at.enfilo.def.security.annotations.Secured;
import at.enfilo.def.security.module.AuthorizationModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mase on 27.10.16.
 */
@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationContainerRequestFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationContainerRequestFilter.class);
    private static final Map<AnnotatedElement, Set<Role>> ROLES_CACHE = new ConcurrentHashMap<>();

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext containerRequestContext)
    throws IOException {

        Class<?> annotatedClass = resourceInfo.getResourceClass();
        Method annotatedMethod = resourceInfo.getResourceMethod();

        String userId = containerRequestContext.getSecurityContext().getUserPrincipal().getName();

        if (!AuthorizationModule.isAuthorized(userId, annotatedClass, annotatedMethod, ROLES_CACHE)) {
            LOGGER.debug("Authorization process failed. {} - [{} | {}]", userId, annotatedClass.getName(), annotatedMethod.getName());

            containerRequestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN).build()
            );
        }
    }
}
