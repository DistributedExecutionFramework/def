package at.enfilo.def.manager.api;

import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.manager.api.thrift.ManagerResponseService;
import at.enfilo.def.manager.api.thrift.ManagerService;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;

import java.util.List;
import java.util.concurrent.Future;

class ManagerServiceClient<T extends ManagerService.Iface, R extends ManagerResponseService.Iface>
implements IManagerServiceClient {

	private final IClient<T> requestClient;
	private final IClient<R> responseClient;
	private final ITicketServiceClient ticketServiceClient;

	public ManagerServiceClient(IClient<T> requestClient, IClient<R> responseClient, ITicketServiceClient ticketServiceClient) {
		this.requestClient = requestClient;
		this.responseClient = responseClient;
		this.ticketServiceClient = ticketServiceClient;
	}

	@Override
	public Future<List<String>> getClusterIds() throws ClientCommunicationException {
		String ticketId = requestClient.execute(T::getClusterIds);

		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketServiceClient)
				.request(R::getClusterIds)
				.via(responseClient);
	}

	@Override
	public Future<ClusterInfoDTO> getClusterInfo(String cId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.getClusterInfo(cId));

		return new TicketFutureBuilder<R, ClusterInfoDTO>()
				.dataTicket(ticketId, ticketServiceClient)
				.request(R::getClusterInfo)
				.via(responseClient);
	}

	@Override
	public Future<ServiceEndpointDTO> getClusterEndpoint(String cId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.getClusterEndpoint(cId));

		return new TicketFutureBuilder<R, ServiceEndpointDTO>()
				.dataTicket(ticketId, ticketServiceClient)
				.request(R::getClusterEndpoint)
				.via(responseClient);
	}

	@Override
	public Future<String> createAWSCluster(int numberOfWorkers, int numberOfReducers, AWSSpecificationDTO awsSpecification) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.createAWSCluster(numberOfWorkers, numberOfReducers, awsSpecification));

		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketServiceClient)
				.request(R::createAWSCluster)
				.via(responseClient);
	}

	@Override
	public Future<Void> addCluster(ServiceEndpointDTO endpoint) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.addCluster(endpoint));

		return new TicketFutureBuilder<>()
				.voidTicket(ticketId, ticketServiceClient);
	}

	@Override
	public Future<Void> deleteCluster(String cId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.destroyCluster(cId));

		return new TicketFutureBuilder<>()
				.voidTicket(ticketId, ticketServiceClient);
	}

	@Override
	public Future<Void> adjustNodePoolSize(String cId, int newNodePoolSize, NodeType nodeType) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.adjustNodePoolSize(cId, newNodePoolSize, nodeType));

		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketServiceClient);
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
