package at.enfilo.def.security.filter;

import at.enfilo.def.security.annotations.Secured;
import at.enfilo.def.security.module.AuthenticationModule;
import at.enfilo.def.security.util.DEFSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Created by mase on 27.10.16.
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationContainerRequestFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationContainerRequestFilter.class);

    @Override
    public void filter(ContainerRequestContext containerRequestContext)
    throws IOException {
        try {

            // Get the HTTP Authorization header from the request.
            String authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            String userId = AuthenticationModule.authenticate(authorizationHeader);

            // Insert userId to SecurityContext (is accessible via @SecurityContext).
            containerRequestContext.setSecurityContext(
                new DEFSecurityContext(userId, containerRequestContext.getSecurityContext())
            );

        } catch (NotAuthorizedException | IllegalStateException e) {
            LOGGER.debug("Authentication process failed.", e);

            // Response with unauthorized status.
            containerRequestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED).build()
            );
        }
    }
}
