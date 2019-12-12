package at.enfilo.def.communication.api.ticket.builder;

import at.enfilo.def.common.api.IThrowingBiFunction;
import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;

import java.util.concurrent.Future;

/**
 * Created by mase on 22.09.2016.
 */
public class ViaBuilder<TClient, TValue>  {

	private final ITicketServiceClient ticketServiceClient;
	private final String ticketId;
    private final IThrowingBiFunction<TClient, String, TValue> subjectProxyFunction;

	public ViaBuilder(
        String ticketId,
        ITicketServiceClient ticketServiceClient,
        IThrowingBiFunction<TClient, String, TValue> subjectProxyFunction
    ) {
        this.ticketId = ticketId;
        this.ticketServiceClient = ticketServiceClient;
        this.subjectProxyFunction = subjectProxyFunction;
    }

    public Future<TValue> via(IClient<TClient> responseClient) {

        // We can use host and port of the response client to build a new ticket status
        // service client as due to server implementation conventions every DEF server
        // should provide access to the {@link TicketStatusService}.
		return new DataTicketFuture<>(
				ticketServiceClient,
				ticketId,
				subjectProxyFunction,
				responseClient::execute
		);
    }
}
