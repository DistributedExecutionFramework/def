package at.enfilo.def.communication.api.ticket.service;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;

class TicketServiceClient implements ITicketServiceClient {
	private final IClient<? extends TicketService.Iface> client;

	TicketServiceClient(IClient<? extends TicketService.Iface> client) {
		this.client = client;
	}

	@Override
	public TicketStatusDTO getTicketStatus(String ticketId) throws ClientCommunicationException {
		return client.execute(client -> client.getTicketStatus(ticketId));
	}

	@Override
	public TicketStatusDTO waitForTicket(String ticketId) throws ClientCommunicationException {
		return client.execute(client -> client.waitForTicket(ticketId));
	}

	@Override
	public TicketStatusDTO cancelTicket(String ticketId, boolean mayInterruptIfRunning) throws ClientCommunicationException {
		return client.execute(client -> client.cancelTicketExecution(ticketId, mayInterruptIfRunning));
	}

	@Override
	public String getFailedMessage(String ticketId) throws ClientCommunicationException {
		return client.execute(client -> client.getFailedMessage(ticketId));
	}

	@Override
	public void close() {
		client.close();
	}
}
