package at.enfilo.def.client.webapp.filter;

import at.enfilo.def.client.util.SessionConstant;
import at.enfilo.def.client.util.WebFace;
import at.enfilo.def.communication.impl.HTTPFilter;
import at.enfilo.def.transfer.dto.AuthDTO;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by mase on 22.08.2016.
 */
@WebFilter({"*.jsf", "*.xhtml", "*.faces", "*.face"})
public class SecurityFilter extends HTTPFilter {

    private static final String RESTRICTED_AREA_PREFIX = WebFace.RESTRICTED_AREA.getRawPath();
    private static final String AUTH_PATH = WebFace.AUTH_FACE.getReference();

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws IOException, ServletException {

        // Check if URL is restricted, then do authentication check, otherwise server the request.
        String path = request.getServletPath();
        if (path.startsWith(RESTRICTED_AREA_PREFIX)) {

            // Trying to retrieve auth information.
            AuthDTO authDTO = (AuthDTO) getSessionAttribute(request, SessionConstant.AUTH_DTO);

            // If this session is associated with some token -> serve.
            if (authDTO == null || authDTO.getUserId() == null || authDTO.getToken() == null) {
                response.sendRedirect(request.getContextPath() + AUTH_PATH);
            }
        }
        filterChain.doFilter(request, response);
    }

    private Object getSessionAttribute(HttpServletRequest request, SessionConstant constant) {
        return request.getSession().getAttribute(constant.getConstant());
    }
}
