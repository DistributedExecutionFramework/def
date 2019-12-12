package at.enfilo.def.communication.api.common.client;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.common.api.IThrowingFunction;
import at.enfilo.def.communication.api.common.util.ServiceReferenceResolver;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.security.filter.AuthorizationHeader;
import at.enfilo.def.transfer.dto.AuthDTO;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import java.util.function.Function;

/**
 * Created by mase on 14.09.2016.
 */
public class ThriftHTTPClient<T extends TServiceClient> extends ServiceClient<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThriftHTTPClient.class);

    private final String serviceReference;
    private final AuthDTO authDTO;
    private final Function<TProtocol, T> clientBuilder;

    public ThriftHTTPClient(ServiceEndpointDTO serviceEndpoint, Class<?> serviceInterfaceClass, Function<TProtocol, T> clientBuilder) {
        this(serviceEndpoint, serviceInterfaceClass.getName(), clientBuilder);
    }

    public ThriftHTTPClient(ServiceEndpointDTO serviceEndpoint, String serviceName, Function<TProtocol, T> clientBuilder) {
        this(serviceEndpoint, null, serviceName, clientBuilder);
    }

    public ThriftHTTPClient(ServiceEndpointDTO serviceEndpoint, AuthDTO authDTO, Class<?> serviceInterfaceClass, Function<TProtocol, T> clientBuilder) {
        this(serviceEndpoint, authDTO, serviceInterfaceClass.getName(), clientBuilder);
    }

    public ThriftHTTPClient(ServiceEndpointDTO serviceEndpoint, AuthDTO authDTO, String serviceName, Function<TProtocol, T> clientBuilder) {
        super(serviceEndpoint);

        this.serviceReference = ServiceReferenceResolver.resolve(serviceName, serviceEndpoint);
        this.authDTO = authDTO;
        this.clientBuilder = clientBuilder;
    }

    @Override
    protected void exec(IThrowingConsumer<T> proxy)
    throws ClientCommunicationException {

        try (THttpClient transport = new THttpClient(serviceReference)) {
            setAuthHeader(transport, authDTO);
            transport.open();

            T serviceClient = getServiceClient(transport, clientBuilder);
            proxy.accept(serviceClient);

        } catch (Exception e) {
            throw new ClientCommunicationException(e);
        }
    }

    @Override
    protected  <R> R exec(IThrowingFunction<T, R> proxy)
    throws ClientCommunicationException {

        try (THttpClient transport = new THttpClient(serviceReference)) {
            setAuthHeader(transport, authDTO);
            transport.open();

            T serviceClient = getServiceClient(transport, clientBuilder);
            return proxy.apply(serviceClient);

        } catch (Exception e) {
            throw new ClientCommunicationException(e);
        }
    }

    private THttpClient setAuthHeader(THttpClient tHttpClient, AuthDTO authDTO) {

        // If authDTO is not null sets "Authorization" header for tHttpClient requests.
        if (authDTO != null && authDTO.isSetToken()) {

            tHttpClient.setCustomHeader(
                HttpHeaders.AUTHORIZATION,
                AuthorizationHeader.formatHeader(authDTO.getToken())
            );
        }
        return tHttpClient;
    }

    private T getServiceClient(TTransport transport, Function<TProtocol, T> clientBuilder)
    throws ClientCommunicationException {

        try {

            if (transport.isOpen()) {

                LOGGER.info("Connected to Thrift HTTP service on serviceReference: {}.", serviceReference);

                TProtocol protocol = new TBinaryProtocol(transport);
                return clientBuilder.apply(protocol);

            } else {

                String clientCommunicationExceptionMessage = String.format(
                    "Not able to connect to Thrift HTTP service on serviceReference: %s.",
                    serviceReference
                );

                throw new ClientCommunicationException(clientCommunicationExceptionMessage);
            }

        } catch(Exception e) {
            throw new ClientCommunicationException(e);
        }
    }

    @Override
    public void close() {
        // TODO
    }
}
