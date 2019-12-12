package at.enfilo.def.execlogic.api;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.builder.TicketFutureBuilder;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.api.ticket.service.TicketServiceClientFactory;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.execlogic.api.thrift.ExecLogicResponseService;
import at.enfilo.def.execlogic.api.thrift.ExecLogicService;
import at.enfilo.def.transfer.dto.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Client Implementation for {@link ExecLogicService} and {@link at.enfilo.def.execlogic.api.rest.IExecLogicService}
 * @param <T> - Request Interface
 * @param <R> - Response Interface
 */
class ExecLogicServiceClient<T extends ExecLogicService.Iface, R extends ExecLogicResponseService.Iface>
		implements IExecLogicServiceClient {

	private static final long POLL_SLEEP = 1000;

	protected IClient<T> requestClient;
	protected IClient<R> responseClient;
	protected ITicketServiceClient ticketClient;

	/**
	 * Constructor for ExecutionServiceClient; requires requestClient and responseClient
	 *
	 * @param requestClient
	 * @param responseClient
	 */
	protected ExecLogicServiceClient(IClient<T> requestClient, IClient<R> responseClient)
	throws ClientCreationException {

		this(requestClient, responseClient, TicketServiceClientFactory.create(requestClient.getServiceEndpoint()));
	}

	/**
	 * Constructor for Unit tests.
	 *
	 * @param requestClient
	 * @param responseClient
	 * @param ticketClient
	 */
	protected ExecLogicServiceClient(
		IClient<T> requestClient,
		IClient<R> responseClient,
		ITicketServiceClient ticketClient
	) {
		this.requestClient = requestClient;
		this.responseClient = responseClient;
		this.ticketClient = ticketClient;
	}

	@Override
	public Future<List<String>> getAllPrograms(String userId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getAllPrograms(userId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getAllPrograms)
				.via(responseClient);
	}

	@Override
	public Future<String> createProgram(String cId, String uId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.createProgram(cId, uId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::createProgram)
				.via(responseClient);
	}

	@Override
	public Future<ProgramDTO> getProgram(String pId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getProgram(pId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, ProgramDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getProgram)
				.via(responseClient);
	}

	@Override
	public Future<Void> deleteProgram(String pId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.deleteProgram(pId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> abortProgram(String pId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.abortProgram(pId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> updateProgramName(String pId, String name) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.updateProgramName(pId, name));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> updateProgramDescription(String pId, String description) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.updateProgramDescription(pId, description));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> markProgramAsFinished(String pId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.markProgramAsFinished(pId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<List<String>> getAllJobs(String pId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getAllJobs(pId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getAllJobs)
				.via(responseClient);
	}

	@Override
	public Future<String> createJob(String pId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.createJob(pId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::createJob)
				.via(responseClient);
	}

	@Override
	public Future<JobDTO> getJob(String pId, String jId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getJob(pId, jId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, JobDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getJob)
				.via(responseClient);
	}

	@Override
	public Future<Void> deleteJob(String pId, String jId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.deleteJob(pId, jId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<String> getAttachedMapRoutine(String pId, String jId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getAttachedMapRoutine(pId, jId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getAttachedMapRoutine)
				.via(responseClient);
	}

	@Override
	public Future<Void> attachMapRoutine(String pId, String jId, String mapRoutineId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.attachMapRoutine(pId, jId, mapRoutineId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<String> getAttachedReduceRoutine(String pId, String jId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getAttachedReduceRoutine(pId, jId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getAttachedReduceRoutine)
				.via(responseClient);
	}

	@Override
	public Future<Void> attachReduceRoutine(String pId, String jId, String reduceRoutineId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.attachReduceRoutine(pId, jId, reduceRoutineId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<List<String>> getAllTasks(String pId, String jId, SortingCriterion sortingCriterion) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getAllTasks(pId, jId, sortingCriterion));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getAllTasks)
				.via(responseClient);
	}

	@Override
	public Future<List<String>> getAllTasksWithState(String pId, String jId, ExecutionState state, SortingCriterion sortingCriterion) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getAllTasksWithState(pId, jId, state, sortingCriterion));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getAllTasksWithState)
				.via(responseClient);
	}

	@Override
	public Future<String> createTask(String pId, String jId, RoutineInstanceDTO objectiveRoutine) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.createTask(pId, jId, objectiveRoutine));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::createTask)
				.via(responseClient);
	}

	@Override
	public Future<TaskDTO> getTask(String pId, String jId, String tId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getTask(pId, jId, tId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, TaskDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getTask)
				.via(responseClient);
	}

	@Override
	public Future<TaskDTO> getTask(String pId, String jId, String tId, boolean includeInParameters, boolean includeOutParameters)
	throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getTaskPartial(pId, jId, tId, includeInParameters, includeOutParameters));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, TaskDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getTaskPartial)
				.via(responseClient);
	}

	@Override
	public Future<Void> markJobAsComplete(String pId, String jId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.markJobAsComplete(pId, jId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> abortJob(String pId, String jId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.abortJob(pId, jId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> abortTask(String pId, String jId, String tId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.abortTask(pId, jId, tId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<Void> reRunTask(String pId, String jId, String tId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.reRunTask(pId, jId, tId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	@Override
	public Future<List<String>> getAllSharedResources(String pId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getAllSharedResources(pId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, List<String>>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getAllSharedResources)
				.via(responseClient);
	}

	@Override
	public Future<String> createSharedResource(String pId, String dataTypeId, ByteBuffer data) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.createSharedResource(pId, dataTypeId, data));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, String>()
				.dataTicket(ticketId, ticketClient)
				.request(R::createSharedResource)
				.via(responseClient);
	}

	@Override
	public Future<ResourceDTO> getSharedResource(String pId, String rId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.getSharedResource(pId, rId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<R, ResourceDTO>()
				.dataTicket(ticketId, ticketClient)
				.request(R::getSharedResource)
				.via(responseClient);
	}

	@Override
	public Future<Void> deleteSharedResource(String pId, String rId) throws ClientCommunicationException {
		String ticketId = requestClient.execute(t -> t.deleteSharedResource(pId, rId));

		// Assembling Future<T> object for received TicketId.
		return new TicketFutureBuilder<>().voidTicket(ticketId, ticketClient);
	}

	/**
	 * Wait for job is either {@link ExecutionState ::SUCESS} or {@link ExecutionState::FAILED}
	 *
	 * @param jId
	 * @return
	 */
	@Override
	public JobDTO waitForJob(String pId, String jId) throws ClientCommunicationException, InterruptedException {
		try {
			JobDTO job = getJob(pId, jId).get();
			while (!((job.getState() == ExecutionState.SUCCESS) || (job.getState() == ExecutionState.FAILED))) {
				Thread.sleep(POLL_SLEEP);
				job = getJob(pId, jId).get();
			}
			return job;
		} catch (ExecutionException e) {
			throw new ClientCommunicationException(e);
		}
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
