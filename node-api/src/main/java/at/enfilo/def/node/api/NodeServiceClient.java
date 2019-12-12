package at.enfilo.def.node.api;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.node.api.thrift.NodeResponseService;
import at.enfilo.def.node.api.thrift.NodeService;
import at.enfilo.def.transfer.dto.*;

import java.util.List;
import java.util.concurrent.Future;

import static at.enfilo.def.communication.dto.TicketStatusDTO.DONE;

public class NodeServiceClient<T extends NodeService.Iface, R extends NodeResponseService.Iface>
implements INodeServiceClient {

	private final IClient<T> requestClient;
	private final IClient<R> responseClient;
	private final ITicketServiceClient ticketClient;

	/**
	 * Constructor used by BaseNodeServiceClientFactory.
	 *
	 * @param requestClient
	 * @param responseClient
	 * @param ticketClient
	 */
	public NodeServiceClient(IClient<T> requestClient, IClient<R> responseClient, ITicketServiceClient ticketClient)
	throws ClientCreationException {
		this.requestClient = requestClient;
		this.responseClient = responseClient;
		this.ticketClient = ticketClient;
	}

	@Override
	public ServiceEndpointDTO getServiceEndpoint() {
		return requestClient.getServiceEndpoint();
	}

	@Override
	public Future<Void> takeControl(String clusterId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.takeControl(clusterId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<NodeInfoDTO> getInfo()
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(T::getInfo);
		return new TicketFutureBuilder<R, NodeInfoDTO>()
			.dataTicket(ticketId, ticketClient)
			.request(R::getInfo)
			.via(responseClient);
	}

	@Override
	public Future<NodeEnvironmentDTO> getEnvironment()
			throws ClientCommunicationException {
		String ticketId = requestClient.execute(T::getEnvironment);
		return new TicketFutureBuilder<R, NodeEnvironmentDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getEnvironment)
				.via(responseClient);
	}

	@Override
	public Future<List<FeatureDTO>> getFeatures() throws ClientCommunicationException {
		String ticketId = requestClient.execute(T::getFeatures);
		return new TicketFutureBuilder<R, List<FeatureDTO>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getFeatures)
				.via(responseClient);
	}

	@Override
	public Future<Void> registerObserver(
		ServiceEndpointDTO endpoint,
		boolean checkPeriodically,
		int periodDuration,
		PeriodUnit periodUnit
	)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.registerObserver(
			endpoint,
			checkPeriodically,
			periodDuration,
			periodUnit
		));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> deregisterObserver(ServiceEndpointDTO endpoint)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.deregisterObserver(endpoint));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> addSharedResource(ResourceDTO sharedResource) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.addSharedResource(sharedResource));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> removeSharedResources(List<String> rIds) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.removeSharedResources(rIds));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public void shutdown()
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(T::shutdown);
		new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public void close() {
		requestClient.close();
		responseClient.close();
		ticketClient.close();
	}
}
