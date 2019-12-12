package at.enfilo.def.communication.api.meta.service;

import at.enfilo.def.communication.api.common.client.*;
import at.enfilo.def.communication.api.meta.rest.IMetaService;
import at.enfilo.def.communication.api.meta.thrift.MetaService;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCreationException;

public class MetaServiceClientFactory {

	public static IMetaServiceClient create(ServiceEndpointDTO serviceEndpoint) throws ClientCreationException {

		IClient<? extends MetaService.Iface> client;
		switch (serviceEndpoint.getProtocol()) {
			case REST:
				client = new RESTClient<>(serviceEndpoint, IMetaService.class);
				break;

			case THRIFT_TCP:
				client = new ThriftTCPClient<>(serviceEndpoint, MetaService.class, MetaService.Client::new);
				break;

			case THRIFT_HTTP:
				client = new ThriftHTTPClient<>(serviceEndpoint, MetaService.class, MetaService.Client::new);
				break;

			case DIRECT:
			default:
				throw new ClientCreationException("Unknown protocol " + serviceEndpoint.getProtocol() + ".");
		}

		return new MetaServiceClient(client);
	}

	public static IMetaServiceClient create(IClient<? extends MetaService.Iface> client) {
		return new MetaServiceClient(client);
	}

	public static IMetaServiceClient createDirectClient(MetaService.Iface metaService) {
		return new MetaServiceClient(new DirectJavaClient<>(metaService));
	}
}
