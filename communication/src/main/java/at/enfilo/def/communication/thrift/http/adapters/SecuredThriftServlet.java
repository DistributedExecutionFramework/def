package at.enfilo.def.communication.thrift.http.adapters;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.security.filter.AuthenticationContainerRequestFilter;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by mase on 31.10.16.
 */
public class SecuredThriftServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationContainerRequestFilter.class);
    private static final String CONTENT_TYPE_THRIFT = "application/x-thrift";

    private final SecuredThriftProcessor securedProcessor;
    private final TProtocolFactory inProtocolFactory;
    private final TProtocolFactory outProtocolFactory;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public <I extends IResource> SecuredThriftServlet(ThriftProcessor<I> processor, TProtocolFactory protocolFactory) {
        this.securedProcessor = SecuredThriftProcessor.wrap(processor);
        this.inProtocolFactory = protocolFactory;
        this.outProtocolFactory = protocolFactory;
    }

    /**
     * @see HttpServlet#HttpServlet()
     */
    public <I extends IResource> SecuredThriftServlet(
        ThriftProcessor<I> processor,
        TProtocolFactory inProtocolFactory,
        TProtocolFactory outProtocolFactory
    ) {
        this.securedProcessor = SecuredThriftProcessor.wrap(processor);
        this.inProtocolFactory = inProtocolFactory;
        this.outProtocolFactory = outProtocolFactory;
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        try {

            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            response.setContentType(CONTENT_TYPE_THRIFT);

            InputStream in = request.getInputStream();
            OutputStream out = response.getOutputStream();

            TTransport transport = new TIOStreamTransport(in, out);

            TProtocol inProtocol = inProtocolFactory.getProtocol(transport);
            TProtocol outProtocol = outProtocolFactory.getProtocol(transport);

            securedProcessor.setAuthorizationHeader(authorizationHeader);
            securedProcessor.process(inProtocol, outProtocol);
            out.flush();

        } catch (TException te) {

            throw new ServletException(te);

        } catch (NotAuthorizedException e) {
            LOGGER.debug("Authentication process failed.", e);

            // Response with unauthorized status.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doPost(request, response);
    }
}
