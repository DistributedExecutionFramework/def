package at.enfilo.def.communication.impl;

import at.enfilo.def.communication.api.IHTTPFilter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * Created by mase on 31.10.16.
 */
public abstract class HTTPFilter implements IHTTPFilter {

    @Override
    public void init(FilterConfig filterConfig)
    throws ServletException {
        // Can be overridden.
    }

    @Override
    public void destroy() {
        // Can be overridden.
    }
}
