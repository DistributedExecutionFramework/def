package at.enfilo.def.node.observer.api.client.factory;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.thrift.NodeObserverService;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeInfoDTO;

import java.util.List;
import java.util.concurrent.Future;

class NodeObserverServiceClient<T extends NodeObserverService.Iface> implements INodeObserverServiceClient {

	private final IClient<T> requestClient;
	private final ITicketServiceClient ticketClient;

	/**
	 * Constructor used by BaseNodeServiceClientFactory.
	 *
	 * @param requestClient
	 * @param responseClient
	 * @param ticketClient
	 */
	public NodeObserverServiceClient(IClient<T> requestClient, IClient responseClient, ITicketServiceClient ticketClient)
	throws ClientCreationException {
		this.requestClient = requestClient;
		this.ticketClient = ticketClient;
	}

	@Override
	public Future<Void> notifyElementsNewState(String nId, List<String> elementIds, ExecutionState newState)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.notifyElementsNewState(nId, elementIds, newState));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> notifyTasksReceived(String nId, List<String> taskIds)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.notifyTasksReceived(nId, taskIds));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> notifyProgramsReceived(String nId, List<String> programIds)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.notifyProgramsReceived(nId, programIds));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> notifyReduceKeysReceived(String nId, String jId, List<String> reduceKeys)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.notifyReduceKeysReceived(nId, jId, reduceKeys));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> notifyNodeInfo(String nId, NodeInfoDTO nodeInfo)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.notifyNodeInfo(nId, nodeInfo));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public ServiceEndpointDTO getServiceEndpoint() {
		return requestClient.getServiceEndpoint();
	}

	@Override
	public void close() {
		requestClient.close();
		ticketClient.close();
	}
}
