package at.enfilo.def.communication.api.ticket.builder;

import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;

/**
 * Created by mase on 22.09.2016.
 */
public class TicketFutureBuilder<TClient, TValue> {

    public RequestBuilder<TClient, TValue> dataTicket(String ticketId, ITicketServiceClient ticketServiceClient) {
        return new RequestBuilder<>(ticketId, ticketServiceClient);
    }

	public VoidTicketFuture voidTicket(String ticketId, ITicketServiceClient ticketServiceClient) {
		return new VoidTicketFuture(ticketServiceClient, ticketId);
	}
}
