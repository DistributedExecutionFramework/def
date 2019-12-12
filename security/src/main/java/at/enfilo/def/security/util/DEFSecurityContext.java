package at.enfilo.def.security.util;


import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

/**
 * Created by mase on 27.10.16.
 */
public class DEFSecurityContext implements SecurityContext {

    private final String userId;
    private final SecurityContext securityContext;

    public DEFSecurityContext(String userId, SecurityContext securityContext) {
        this.userId = userId;
        this.securityContext = securityContext;
    }

    public static DEFSecurityContext empty() {
        return new DEFSecurityContext(null, null);
    }

    @Override
    public Principal getUserPrincipal() {
        return () -> userId;
    }

    @Override
    public boolean isUserInRole(String s) {
        // TODO implement roles logic if necessary.
        return true;
    }

    @Override
    public boolean isSecure() {
        return securityContext != null && securityContext.isSecure();
    }

    @Override
    public String getAuthenticationScheme() {
        return securityContext != null ? securityContext.getAuthenticationScheme() : null;
    }
}
