package at.enfilo.def.communication.api;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by mase on 22.08.2016.
 */
public interface IHTTPFilter extends Filter {

    String DEFAULT_CHARACTER_ENCODING = "UTF-8";

    @Override
    default void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
    throws IOException, ServletException {
        request.setCharacterEncoding(DEFAULT_CHARACTER_ENCODING);
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, filterChain);
    }

    void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws IOException, ServletException;
}

