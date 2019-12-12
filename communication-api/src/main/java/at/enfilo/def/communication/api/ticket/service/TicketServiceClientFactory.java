package at.enfilo.def.communication.api.ticket.service;


import at.enfilo.def.communication.api.common.client.*;
import at.enfilo.def.communication.api.ticket.rest.ITicketService;
import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCreationException;

public class TicketServiceClientFactory {

	public static ITicketServiceClient create(ServiceEndpointDTO serviceEndpoint) throws ClientCreationException {

		IClient<? extends TicketService.Iface> client;
		switch (serviceEndpoint.getProtocol()) {
			case REST:
				client = new RESTClient<>(serviceEndpoint, ITicketService.class);
				break;

			case THRIFT_TCP:
				client = new ThriftTCPClient<>(serviceEndpoint, TicketService.class, TicketService.Client::new);
				break;

			case THRIFT_HTTP:
				client = new ThriftHTTPClient<>(serviceEndpoint, TicketService.class, TicketService.Client::new);
				break;

			case DIRECT:
			default:
				throw new ClientCreationException("Unknown protocol " + serviceEndpoint.getProtocol() + ".");
		}

		return new TicketServiceClient(client);
	}

	public static ITicketServiceClient create(IClient<? extends TicketService.Iface> client) {
		return new TicketServiceClient(client);
	}

	public static ITicketServiceClient createDirectClient(TicketService.Iface ticketService) {
		return new TicketServiceClient(new DirectJavaClient<>(ticketService));
	}
}
