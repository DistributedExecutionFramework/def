package at.enfilo.def.communication.api.common.factory;

import at.enfilo.def.communication.api.common.client.*;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.api.ticket.service.TicketServiceClientFactory;
import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.transfer.dto.AuthDTO;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.protocol.TProtocol;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by mase on 20.09.2016.
 */
public class UnifiedClientFactory<T extends IServiceClient> {

    private static final Map<Class<? extends IServiceClient>, Class<? extends IResource>> CLIENT_REST_REQUEST_MAP = new HashMap<>();
    private static final Map<Class<? extends IServiceClient>, Class<? extends IResource>> CLIENT_REST_RESPONSE_MAP = new HashMap<>();

    private static final Map<Class<? extends IServiceClient>, Class<?>> CLIENT_THRIFT_REQUEST_MAP = new HashMap<>();
    private static final Map<Class<? extends IServiceClient>, Class<?>> CLIENT_THRIFT_RESPONSE_MAP = new HashMap<>();

    private static final Map<Class<? extends IServiceClient>, IServiceClientFactoryFunction<IClient, IClient, ?>> CLIENT_FACTORY_FUNCTION_MAP = new HashMap<>();

    private static final Map<Class<? extends IServiceClient>, Function<TProtocol, ? extends TServiceClient>> THRIFT_CLIENT_REQUEST_BUILDER_MAP = new HashMap<>();
    private static final Map<Class<? extends IServiceClient>, Function<TProtocol, ? extends TServiceClient>> THRIFT_CLIENT_RESPONSE_BUILDER_MAP = new HashMap<>();

    private final Class<T> serviceClientInterfaceClass;

    protected UnifiedClientFactory(Class<T> serviceClientInterfaceClass) {
        this.serviceClientInterfaceClass = serviceClientInterfaceClass;
    }

	/**
	 * Create Client for a given {@link ServiceEndpointDTO} endpoint.
	 *
	 * @param serviceEndpoint
	 * @return
	 * @throws ClientCreationException
	 */
	public T createClient(ServiceEndpointDTO serviceEndpoint) throws ClientCreationException {
    	ITicketServiceClient ticketServiceClient = TicketServiceClientFactory.create(serviceEndpoint);
		return createClient(serviceEndpoint, ticketServiceClient, null);
	}

	/**
	 * Create Client for a given {@link ServiceEndpointDTO} and {@link AuthDTO} token.
	 *
	 * @param serviceEndpoint - endpoint
	 * @param auth - authentication token
	 * @return
	 * @throws ClientCreationException
	 */
	public T createClient(ServiceEndpointDTO serviceEndpoint, AuthDTO auth) throws ClientCreationException {
		ITicketServiceClient ticketServiceClient = TicketServiceClientFactory.create(serviceEndpoint);
		return createClient(serviceEndpoint, ticketServiceClient, auth);
	}


	/**
	 * Create client with given {@link ServiceEndpointDTO} and {@link ITicketServiceClient} and {@link AuthDTO} as token.
	 *
	 * @param serviceEndpoint - service endpoint
	 * @param ticketServiceClient - ticket service client
	 * @param auth - authentication token
	 * @return
	 * @throws ClientCreationException
	 */
	public T createClient(ServiceEndpointDTO serviceEndpoint, ITicketServiceClient ticketServiceClient, AuthDTO auth)
	throws ClientCreationException {

		switch (serviceEndpoint.getProtocol()) {
			case REST:
				return createRESTClient(serviceEndpoint, ticketServiceClient, auth, serviceClientInterfaceClass);
			case THRIFT_HTTP:
				return createThriftHTTPClient(serviceEndpoint, ticketServiceClient, auth, serviceClientInterfaceClass);
			case THRIFT_TCP:
				return createThriftTCPClient(serviceEndpoint, ticketServiceClient, serviceClientInterfaceClass);
			default:
				throw new ClientCreationException("Protocol " + serviceEndpoint.getProtocol() + " not supported.");
		}
	}


	public T createDirectClient(
		Object requestService,
		Object responseService,
		TicketService.Iface ticketService,
		Class<T> unifiedClientClass
	) throws ClientCreationException {

		IClient<?> requestServiceClient = new DirectJavaClient<>(requestService);
		IClient<?> responseServiceClient = new DirectJavaClient<>(responseService);
		IClient<TicketService.Iface> ticketClient = new DirectJavaClient<>(ticketService);
		IServiceClientFactoryFunction<IClient, IClient, ?> factoryFunction = CLIENT_FACTORY_FUNCTION_MAP.get(unifiedClientClass);
		return unifiedClientClass.cast(factoryFunction.apply(requestServiceClient, responseServiceClient, TicketServiceClientFactory.create(ticketClient)));
	}


    private T createRESTClient(
		ServiceEndpointDTO serviceEndpoint,
		ITicketServiceClient ticketServiceClient,
		AuthDTO auth,
		Class<T> unifiedClientClass
	) throws ClientCreationException {

        // Assembling REST client for request interface.
        Class<? extends IResource> restRequestInterfaceClass = CLIENT_REST_REQUEST_MAP.get(unifiedClientClass);
        IClient<?> restRequestClient = new RESTClient<>(serviceEndpoint, auth, restRequestInterfaceClass);

        // Assembling REST client for response interface.
        Class<? extends IResource> restResponseInterfaceClass = CLIENT_REST_RESPONSE_MAP.get(unifiedClientClass);
		IClient<?> restResponseClient = null;
        if (restResponseInterfaceClass != null) {
			restResponseClient = new RESTClient<>(serviceEndpoint, restResponseInterfaceClass);
		}

        // Assembling factory function for unified client.
		IServiceClientFactoryFunction<IClient, IClient, ?> factoryFunction = CLIENT_FACTORY_FUNCTION_MAP.get(unifiedClientClass);
        return unifiedClientClass.cast(factoryFunction.apply(restRequestClient, restResponseClient, ticketServiceClient));
    }


    private T createThriftTCPClient(
		ServiceEndpointDTO serviceEndpoint,
		ITicketServiceClient ticketServiceClient,
		Class<T> unifiedClientClass
	) throws ClientCreationException {

		// Assembling ThriftTCP client for request interface.
		Class<?> thriftRequestInterfaceClass = CLIENT_THRIFT_REQUEST_MAP.get(unifiedClientClass);
		Function<TProtocol, ? extends TServiceClient> requestClientBuilder = THRIFT_CLIENT_REQUEST_BUILDER_MAP.get(unifiedClientClass);

		IClient<?> thriftRequestClient = new ThriftTCPClient<>(serviceEndpoint, thriftRequestInterfaceClass, requestClientBuilder);

		// Assembling ThriftTCP client for response interface.
		Class<?> thriftResponseInterfaceClass = CLIENT_THRIFT_RESPONSE_MAP.get(unifiedClientClass);
		Function<TProtocol, ? extends TServiceClient> responseClientBuilder = THRIFT_CLIENT_RESPONSE_BUILDER_MAP.get(unifiedClientClass);
		IClient<?> thriftResponseClient = null;
		if (thriftResponseInterfaceClass != null) {
			thriftResponseClient = new ThriftTCPClient<>(serviceEndpoint, thriftResponseInterfaceClass, responseClientBuilder);
		}

        // Assembling factory function for unified client.
		IServiceClientFactoryFunction<IClient, IClient, ?> factoryFunction = CLIENT_FACTORY_FUNCTION_MAP.get(unifiedClientClass);
        return unifiedClientClass.cast(factoryFunction.apply(thriftRequestClient, thriftResponseClient, ticketServiceClient));
    }


    private T createThriftHTTPClient(
		ServiceEndpointDTO serviceEndpoint,
		ITicketServiceClient ticketServiceClient,
		AuthDTO auth,
		Class<T> unifiedClientClass
	) throws ClientCreationException {

        // Assembling ThriftTCP client for request interface.
        Class<?> thriftRequestInterfaceClass = CLIENT_THRIFT_REQUEST_MAP.get(unifiedClientClass);
        Function<TProtocol, ? extends TServiceClient> requestClientBuilder = THRIFT_CLIENT_REQUEST_BUILDER_MAP.get(unifiedClientClass);

        IClient<?> thriftRequestClientClass = new ThriftHTTPClient<>(serviceEndpoint, auth, thriftRequestInterfaceClass, requestClientBuilder);

        // Assembling ThriftTCP client for response interface.
        Class<?> thriftResponseInterfaceClass = CLIENT_THRIFT_RESPONSE_MAP.get(unifiedClientClass);
        Function<TProtocol, ? extends TServiceClient> responseClientBuilder = THRIFT_CLIENT_RESPONSE_BUILDER_MAP.get(
            unifiedClientClass
        );
        IClient<?> thriftResponseClient = null;
        if (thriftResponseInterfaceClass != null) {
        	thriftResponseClient = new ThriftHTTPClient<>(serviceEndpoint, thriftResponseInterfaceClass, responseClientBuilder);
        }

        // Assembling factory function for unified client.
		IServiceClientFactoryFunction<IClient, IClient, ?> factoryFunction = CLIENT_FACTORY_FUNCTION_MAP.get(unifiedClientClass);
        return unifiedClientClass.cast(factoryFunction.apply(thriftRequestClientClass, thriftResponseClient, ticketServiceClient));
    }

    protected static void register(
        Class<? extends IServiceClient> unifiedClientClass,
        IServiceClientFactoryFunction<IClient, IClient, ?> unifiedClientFactory,
        Class<? extends IResource> restRequestClass,
        Class<? extends IResource> restResponseClass,
        Class<?> thriftRequestClass,
        Function<TProtocol, ? extends TServiceClient> thriftRequestClientBuilder,
        Class<?> thriftResponseClass,
        Function<TProtocol, ? extends TServiceClient> thriftResponseClientBuilder
    ) {

        CLIENT_REST_REQUEST_MAP.put(unifiedClientClass, restRequestClass);
        CLIENT_REST_RESPONSE_MAP.put(unifiedClientClass, restResponseClass);

        CLIENT_THRIFT_REQUEST_MAP.put(unifiedClientClass, thriftRequestClass);
        THRIFT_CLIENT_REQUEST_BUILDER_MAP.put(unifiedClientClass, thriftRequestClientBuilder);

        CLIENT_THRIFT_RESPONSE_MAP.put(unifiedClientClass, thriftResponseClass);
        THRIFT_CLIENT_RESPONSE_BUILDER_MAP.put(unifiedClientClass, thriftResponseClientBuilder);

        CLIENT_FACTORY_FUNCTION_MAP.put(unifiedClientClass, unifiedClientFactory);
    }

	protected static void register(
		Class<? extends IServiceClient> unifiedClientClass,
		IServiceClientFactoryFunction<IClient, IClient, ?> unifiedClientFactory,
		Class<? extends IResource> restRequestClass,
		Class<?> thriftRequestClass,
		Function<TProtocol, ? extends TServiceClient> thriftRequestClientBuilder
	) {

		CLIENT_REST_REQUEST_MAP.put(unifiedClientClass, restRequestClass);

		CLIENT_THRIFT_REQUEST_MAP.put(unifiedClientClass, thriftRequestClass);
		THRIFT_CLIENT_REQUEST_BUILDER_MAP.put(unifiedClientClass, thriftRequestClientBuilder);

		CLIENT_FACTORY_FUNCTION_MAP.put(unifiedClientClass, unifiedClientFactory);
	}
}
