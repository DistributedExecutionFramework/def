package at.enfilo.def.communication.api.ticket.builder;

import at.enfilo.def.common.api.IThrowingBiFunction;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;

/**
 * Created by mase on 22.09.2016.
 */
public class RequestBuilder<TClient, TValue> {

    private final ITicketServiceClient ticketServiceClient;
    private final String ticketId;

    public RequestBuilder(
        String ticketId,
        ITicketServiceClient ticketServiceClient
    ) {
        this.ticketId = ticketId;
        this.ticketServiceClient = ticketServiceClient;
    }

    public ViaBuilder<TClient, TValue> request(IThrowingBiFunction<TClient, String, TValue> subjectProxyFunction) {
        return new ViaBuilder<>(
                ticketId,
                ticketServiceClient,
				subjectProxyFunction
		);
    }
}
