package at.enfilo.def.security.module;

import at.enfilo.def.security.util.DEFTokenStorage;

import javax.ws.rs.NotAuthorizedException;

import static at.enfilo.def.security.filter.AuthorizationHeader.AUTHORIZATION_HEADER_PREFIX;

/**
 * Created by mase on 31.10.16.
 */
public class AuthenticationModule {

    private AuthenticationModule() {
        // Hiding public constructor
    }

    /**
     * Tries to authenticate user by token from the given authorizationHeader.
     *
     * @param authorizationHeader header that stores token.
     * @return userId if header is properly formatted and extracted token is valid.
     * @throws NotAuthorizedException
     */
    public static String authenticate(String authorizationHeader)
    throws NotAuthorizedException {

        // Check if the HTTP Authorization header is present and formatted correctly.
        if (authorizationHeader != null && authorizationHeader.startsWith(AUTHORIZATION_HEADER_PREFIX)) {
            // Extract the token from the HTTP Authorization header.
            String token = authorizationHeader.substring(AUTHORIZATION_HEADER_PREFIX.length()).trim();

            // Validate the token.
            if (DEFTokenStorage.getInstance().isValidToken(token)) {
                // Request associated user.
                return DEFTokenStorage.getInstance().getAssociatedUserId(token);
            }
            else throw new NotAuthorizedException("Supplied token is not valid.");
        }
        else throw new NotAuthorizedException("Authorization header must be specified.");
    }
}
