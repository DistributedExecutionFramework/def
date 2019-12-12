package at.enfilo.def.scheduler.api;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.scheduler.api.thrift.SchedulerResponseService;
import at.enfilo.def.scheduler.api.thrift.SchedulerService;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;

import java.util.List;
import java.util.concurrent.Future;

class SchedulerServiceClient<T extends SchedulerService.Iface, R extends SchedulerResponseService.Iface>
implements ISchedulerServiceClient {

	private final IClient<T> requestClient;
	private final IClient<R> responseClient;
	private final ITicketServiceClient ticketClient;

	/**
	 * Constructor used by SchedulerServiceClientFactory.
	 *
	 * @param requestClient - request client
	 * @param responseClient - response client
	 * @param ticketClient - ticket clinet
	 */
	public SchedulerServiceClient(IClient<T> requestClient, IClient<R> responseClient, ITicketServiceClient ticketClient)  {
		this.requestClient = requestClient;
		this.responseClient = responseClient;
		this.ticketClient = ticketClient;
	}

	@Override
	public ServiceEndpointDTO getServiceEndpoint() {
		return requestClient.getServiceEndpoint();
	}

	@Override
	public Future<Void> addJob(String jId)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.addJob(jId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> scheduleTask(String jId, TaskDTO task)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.scheduleTask(jId, task));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> markJobAsComplete(String jId)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.markJobAsComplete(jId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> removeJob(String jId)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.removeJob(jId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> addWorker(String nId, ServiceEndpointDTO endpoint)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.addWorker(nId, endpoint));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> removeWorker(String nId)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.removeWorker(nId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> addReducer(String nId, ServiceEndpointDTO endpoint)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.addReducer(nId, endpoint));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> removeReducer(String nId)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.removeReducer(nId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> extendToReduceJob(String jId, String reduceRoutineId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.extendToReduceJob(jId, reduceRoutineId));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> scheduleResource(String jId, List<ResourceDTO> resources) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.scheduleReduce(jId, resources));
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<List<ResourceDTO>> finalizeReduce(String jId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.finalizeReduce(jId));
		return new TicketFutureBuilder<R, List<ResourceDTO>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::finalizeReduce)
				.via(responseClient);
	}

	@Override
	public void close() {
		requestClient.close();
		responseClient.close();
		ticketClient.close();
	}

}
