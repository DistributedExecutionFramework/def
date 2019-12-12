package at.enfilo.def.security.filter;

/**
 * Created by mase on 27.10.16.
 */
public class AuthorizationHeader {

    public static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER_FORMAT = AUTHORIZATION_HEADER_PREFIX + "%s";

    private AuthorizationHeader() {
        // Hiding public constructor
    }

    /**
     * Formats auth header.
     *
     * @param authToken token to be used for formatting.
     * @return formatted header.
     */
    public static String formatHeader(String authToken) {
        return String.format(AUTHORIZATION_HEADER_FORMAT, authToken);
    }
}
