package at.enfilo.def.reducer.api;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.node.api.NodeServiceClient;
import at.enfilo.def.reducer.api.thrift.ReducerResponseService;
import at.enfilo.def.reducer.api.thrift.ReducerService;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.List;
import java.util.concurrent.Future;

import static at.enfilo.def.communication.dto.TicketStatusDTO.DONE;


class ReducerServiceClient<T extends ReducerService.Iface, R extends ReducerResponseService.Iface>
extends NodeServiceClient<T, R> implements IReducerServiceClient {

	private final IClient<T> requestClient;
	private final IClient<R> responseClient;
	private final ITicketServiceClient ticketClient;

	ReducerServiceClient(IClient<T> requestClient, IClient<R> responseClient, ITicketServiceClient ticketClient)
	throws ClientCreationException {
		super(requestClient, responseClient, ticketClient);

		this.requestClient = requestClient;
		this.responseClient = responseClient;
		this.ticketClient = ticketClient;
	}

	/**
	 * Returns service endpoint of this client.
	 *
	 * @return ServiceEndpoint instance
	 */
	@Override
	public ServiceEndpointDTO getServiceEndpoint() {
		return requestClient.getServiceEndpoint();
	}

	@Override
	public Future<Void> createReduceJob(String jId, String routineId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.createReduceJob(jId, routineId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> deleteReduceJob(String jId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.deleteReduceJob(jId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> add(String jId, List<ResourceDTO> resources) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.add(jId, resources));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> reduce(String jId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.reduce(jId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<List<ResourceDTO>> fetchResult(String jId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.fetchResult(jId));
		return new TicketFutureBuilder<R, List<ResourceDTO>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::fetchResult)
				.via(responseClient);
	}
}
