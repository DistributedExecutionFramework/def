package at.enfilo.def.communication.api.common.factory;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.exception.ClientCreationException;

/**
 * Factory creation function for service clients with request, response and ticketing.
 *
 * @param <T> Request Client Type - should be {@link IClient <T>}
 * @param <R> Response Client Type - should be {@link IClient<R>}
 * @param <V> Type of Client
 */
@FunctionalInterface
public interface IServiceClientFactoryFunction<T, R, V> {
	V apply(T requestClient, R responseClient, ITicketServiceClient ticketClient) throws ClientCreationException;
}
