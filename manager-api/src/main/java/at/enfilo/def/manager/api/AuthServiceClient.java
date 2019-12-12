package at.enfilo.def.manager.api;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.manager.api.thrift.AuthResponseService;
import at.enfilo.def.manager.api.thrift.AuthService;
import at.enfilo.def.transfer.dto.AuthDTO;

import java.util.concurrent.Future;

class AuthServiceClient<T extends AuthService.Iface, R extends AuthResponseService.Iface>
implements IAuthServiceClient {

	private final IClient<T> requestClient;
	private final IClient<R> responseClient;
	private final ITicketServiceClient ticketServiceClient;

	public AuthServiceClient(IClient<T> requestClient, IClient<R> responseClient, ITicketServiceClient ticketServiceClient) {
		this.requestClient = requestClient;
		this.responseClient = responseClient;
		this.ticketServiceClient = ticketServiceClient;
	}

	@Override
	public Future<AuthDTO> getToken(String name, String password) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.getToken(name, password));

		return new TicketFutureBuilder<R, AuthDTO>()
				.dataTicket(ticketId, ticketServiceClient)
				.request(R::getToken)
				.via(responseClient);
	}


	@Override
	public ServiceEndpointDTO getServiceEndpoint() {
		return requestClient.getServiceEndpoint();
	}

	@Override
	public void close() {
		requestClient.close();
		responseClient.close();
		ticketServiceClient.close();
	}
}
