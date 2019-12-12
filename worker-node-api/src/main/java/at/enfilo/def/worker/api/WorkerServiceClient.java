package at.enfilo.def.worker.api;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.node.api.NodeServiceClient;
import at.enfilo.def.transfer.dto.QueueInfoDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.thrift.WorkerResponseService;
import at.enfilo.def.worker.api.thrift.WorkerService;

import java.util.List;
import java.util.concurrent.Future;

import static at.enfilo.def.communication.dto.TicketStatusDTO.DONE;

/**
 *
 * @param <T> - Request Interface
 * @param <R> - Response Interface
 */
class WorkerServiceClient<T extends WorkerService.Iface, R extends WorkerResponseService.Iface> extends NodeServiceClient<T, R>
implements IWorkerServiceClient {
	private final IClient<T> requestClient;
	private final IClient<R> responseClient;
	private final ITicketServiceClient ticketClient;

	WorkerServiceClient(IClient<T> requestClient, IClient<R> responseClient, ITicketServiceClient ticketClient) throws ClientCreationException {
		super(requestClient, responseClient, ticketClient);

		this.requestClient = requestClient;
		this.responseClient = responseClient;
		this.ticketClient = ticketClient;
	}

	@Override
	public Future<List<String>> getQueues()
			throws ClientCommunicationException {
		String ticketId = requestClient.execute(T::getQueues);
		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getQueues)
				.via(responseClient);
	}

	@Override
	public Future<QueueInfoDTO> getQueueInfo(String qId)
			throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.getQueueInfo(qId));
		return new TicketFutureBuilder<R, QueueInfoDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getQueueInfo)
				.via(responseClient);
	}

	@Override
	public Future<Void> createQueue(String qId)
			throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.createQueue(qId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> deleteQueue(String qId)
			throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.deleteQueue(qId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> releaseQueue(String qId)
			throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.releaseQueue(qId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<List<String>> getQueuedTasks(String qId)
			throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.getQueuedTasks(qId));
		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getQueuedTasks)
				.via(responseClient);
	}

	@Override
	public Future<Void> queueTasks(String qId, List<TaskDTO> taskList)
			throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.queueTasks(qId, taskList));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> pauseQueue(String qId)
			throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.pauseQueue(qId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> moveTasks(
			String queueId,
			List<String> taskIds,
			ServiceEndpointDTO targetNodeEndpoint
	) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.moveTasks(queueId, taskIds, targetNodeEndpoint));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> moveAllTasks(ServiceEndpointDTO targetNodeEndpoint)
			throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.moveAllTasks(targetNodeEndpoint));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<TaskDTO> fetchFinishedTask(String tId)
			throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.fetchFinishedTask(tId));
		return new TicketFutureBuilder<R, TaskDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::fetchFinishedTask)
				.via(responseClient);
	}

	@Override
	public Future<Void> abortTask(String tId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.abortTask(tId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}


	@Override
	public Future<Void> setStoreRoutine(String routineId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.setStoreRoutine(routineId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<String> getStoreRoutine() throws ClientCommunicationException {
		String ticketId = requestClient.execute(c -> c.getStoreRoutine());
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getStoreRoutine)
				.via(responseClient);
	}

	@Override
	public ServiceEndpointDTO getServiceEndpoint() {
		return requestClient.getServiceEndpoint();
	}
}
