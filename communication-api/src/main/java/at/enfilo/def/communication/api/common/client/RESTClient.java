package at.enfilo.def.communication.api.common.client;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.common.api.IThrowingFunction;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.api.common.util.ServiceReferenceResolver;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.security.filter.AuthorizationHeader;
import at.enfilo.def.transfer.dto.AuthDTO;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mase on 31.08.2016.
 */
public class RESTClient<T extends IResource> extends ServiceClient<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RESTClient.class);

    private final String serviceReference;
    private final Class<T> resourceInterfaceClass;
    private final AuthDTO authDTO;

    private T resourceClient;

    public RESTClient(ServiceEndpointDTO serviceEndpoint, Class<T> resourceInterfaceClass) {
        this(serviceEndpoint, null, resourceInterfaceClass);
    }

    public RESTClient(ServiceEndpointDTO serviceEndpoint, AuthDTO authDTO, Class<T> resourceInterfaceClass) {
        super(serviceEndpoint);

        this.serviceReference = ServiceReferenceResolver.resolve(serviceEndpoint);
        this.resourceInterfaceClass = resourceInterfaceClass;
        this.authDTO = authDTO;
    }

    @Override
    protected void exec(IThrowingConsumer<T> proxy)
    throws ClientCommunicationException {
        try {

            if (resourceClient == null) resourceClient = getResourceClient(
                serviceReference,
                getAuthHeaderMap(authDTO),
                resourceInterfaceClass
            );

            proxy.accept(resourceClient);

        } catch (Exception e) {
            throw new ClientCommunicationException(e);
        }
    }

    @Override
    protected  <R> R exec(IThrowingFunction<T, R> proxy)
    throws ClientCommunicationException {
        try {

            if (resourceClient == null) resourceClient = getResourceClient(
                serviceReference,
                getAuthHeaderMap(authDTO),
                resourceInterfaceClass
            );

            return proxy.apply(resourceClient);

        } catch (Exception e) {
            throw new ClientCommunicationException(e);
        }
    }

    private Map<String, String> getAuthHeaderMap(AuthDTO authDTO) {

        // If authDTO is not null assembles authMap that contains "Authorization" header for CXF client requests.
        Map<String, String> authMap = new HashMap<>();
        if (authDTO != null && authDTO.isSetToken()) {

            authMap.put(
                HttpHeaders.AUTHORIZATION,
                AuthorizationHeader.formatHeader(authDTO.getToken())
            );
        }
        return authMap;
    }

    private T getResourceClient(String address, Map<String, String> headersMap, Class<T> resourceInterfaceClass)
    throws ClientCommunicationException {
        try {

            LOGGER.info("Fetching resource for location: {}.", address);

            JAXRSClientFactoryBean clientFactory = new JAXRSClientFactoryBean();
            clientFactory.setAddress(address);
            clientFactory.setHeaders(headersMap);
            clientFactory.setProvider(new JacksonJsonProvider());
            clientFactory.setServiceClass(resourceInterfaceClass);

            return clientFactory.create(resourceInterfaceClass);

        } catch (Exception e) {
            throw new ClientCommunicationException(e);
        }
    }

    @Override
    public void close() {
        // TODO
    }
}
