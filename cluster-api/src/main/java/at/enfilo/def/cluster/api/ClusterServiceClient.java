package at.enfilo.def.cluster.api;

import at.enfilo.def.cluster.api.thrift.ClusterResponseService;
import at.enfilo.def.cluster.api.thrift.ClusterService;
import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;

import java.util.List;
import java.util.concurrent.Future;

class ClusterServiceClient<T extends ClusterService.Iface, R extends ClusterResponseService.Iface>
implements IClusterServiceClient {

	private final IClient<T> requestClient;
	private final IClient<R> responseClient;
	private final ITicketServiceClient ticketClient;

	/**
	 * Constructor used by ClusterServiceClientFactory.
	 *
	 * @param requestClient
	 * @param responseClient
	 * @param ticketClient
	 */
	public ClusterServiceClient(IClient<T> requestClient, IClient<R> responseClient, ITicketServiceClient ticketClient)
	throws ClientCreationException {
		this.requestClient = requestClient;
		this.responseClient = responseClient;
		this.ticketClient = ticketClient;
	}

	@Override
	public Future<Void> takeControl(String managerId)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.takeControl(managerId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<ClusterInfoDTO> getClusterInfo()
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(T::getClusterInfo);

		return new TicketFutureBuilder<R, ClusterInfoDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getClusterInfo)
				.via(responseClient);
	}

	@Override
	public void destroyCluster()
	throws ClientCommunicationException {
		requestClient.executeVoid(T::destroyCluster);
	}

	@Override
	public Future<List<String>> getAllNodes(NodeType type)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getAllNodes(type));

		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getAllNodes)
				.via(responseClient);
	}

	@Override
	public Future<NodeInfoDTO> getNodeInfo(String nId)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getNodeInfo(nId));

		return new TicketFutureBuilder<R, NodeInfoDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getNodeInfo)
				.via(responseClient);
	}

	@Override
	public Future<List<FeatureDTO>> getEnvironment() throws ClientCommunicationException {
		String ticketId = requestClient.execute(ClusterService.Iface::getEnvironment);

		return new TicketFutureBuilder<R, List<FeatureDTO>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getEnvironment)
				.via(responseClient);
	}

	@Override
	public Future<List<FeatureDTO>> getNodeEnvironment(String nId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getNodeEnvironment(nId));

		return new TicketFutureBuilder<R, List<FeatureDTO>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getNodeEnvironment)
				.via(responseClient);
	}

	@Override
	public Future<Void> addNode(ServiceEndpointDTO serviceEndpoint, NodeType type)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.addNode(serviceEndpoint, type));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}


	@Override
	public Future<Void> removeNode(String nId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.removeNode(nId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<ServiceEndpointDTO> getNodeServiceEndpoint(String nId)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getNodeServiceEndpoint(nId));
		return new TicketFutureBuilder<R, ServiceEndpointDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getNodeServiceEndpoint)
				.via(responseClient);
	}

	@Override
	public Future<ServiceEndpointDTO> getSchedulerServiceEndpoint(NodeType clusterNodeType)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getSchedulerServiceEndpoint(clusterNodeType));
		return new TicketFutureBuilder<R, ServiceEndpointDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getSchedulerServiceEndpoint)
				.via(responseClient);
	}

	@Override
	public Future<Void> setSchedulerServiceEndpoint(
		NodeType clusterNodeType,
		ServiceEndpointDTO schedulerServiceEndpoint
	) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.setSchedulerServiceEndpoint(
			clusterNodeType,
			schedulerServiceEndpoint
		));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<String> getStoreRoutine()
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(T::getStoreRoutine);
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)

				.request(R::getStoreRoutine)
				.via(responseClient);
	}

	@Override
	public Future<Void> setStoreRoutine(String routineId)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.setStoreRoutine(routineId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<String> getDefaultMapRoutine()
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(T::getDefaultMapRoutine);
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getDefaultMapRoutine)
				.via(responseClient);
	}

	@Override
	public Future<Void> setDefaultMapRoutine(String routineId)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.setDefaultMapRoutine(routineId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<ServiceEndpointDTO> getNodeServiceEndpointConfiguration(NodeType nodeType) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getNodeServiceEndpointConfiguration(nodeType));
		return new TicketFutureBuilder<R, ServiceEndpointDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getNodeServiceEndpointConfiguration)
				.via(responseClient);
	}

	@Override
	public Future<ServiceEndpointDTO> getLibraryEndpointConfiguration() throws ClientCommunicationException {
		String ticketId = requestClient.execute(T::getLibraryEndpointConfiguration);
		return new TicketFutureBuilder<R, ServiceEndpointDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getLibraryEndpointConfiguration)
				.via(responseClient);
	}

	@Override
	public Future<List<String>> findNodesForShutdown(NodeType nodeType, int nrOfNodesToShutdown) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.findNodesForShutdown(nodeType, nrOfNodesToShutdown));
		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::findNodesForShutdown)
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
		ticketClient.close();
	}
}
