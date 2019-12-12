package at.enfilo.def.execlogic.api;

import at.enfilo.def.communication.api.common.client.DirectJavaClient;
import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.service.TicketServiceClientFactory;
import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.execlogic.api.rest.IExecLogicResponseService;
import at.enfilo.def.execlogic.api.rest.IExecLogicService;
import at.enfilo.def.transfer.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

public class ExecLogicServiceClientTest {
	private IExecLogicServiceClient client;
	private IExecLogicService requestServiceMock;
	private IExecLogicResponseService responseServiceMock;
	private TicketService.Iface ticketServiceMock;
	private IClient<IExecLogicService> requestClient;
	private IClient<IExecLogicResponseService> responseClient;
	private Random rnd;

	@Before
	public void setUp() throws Exception {
		rnd = new Random();
		requestServiceMock = Mockito.mock(IExecLogicService.class);
		responseServiceMock = Mockito.mock(IExecLogicResponseService.class);
		ticketServiceMock = Mockito.mock(TicketService.Iface.class);
		requestClient = new DirectJavaClient<>(requestServiceMock);
		responseClient = new DirectJavaClient<>(responseServiceMock);
		IClient<TicketService.Iface> ticketClient = new DirectJavaClient<>(ticketServiceMock);
		client = new ExecLogicServiceClient<>(requestClient, responseClient, TicketServiceClientFactory.create(ticketClient));
	}

	@Test
	public void createProgram() throws Exception {
		String cId = UUID.randomUUID().toString();
		String uId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String pId = UUID.randomUUID().toString();
		when(requestServiceMock.createProgram(cId, uId)).thenReturn(ticketId);
		when(responseServiceMock.createProgram(ticketId)).thenReturn(pId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<String> newProgram = client.createProgram(cId, uId);
		assertEquals(pId, newProgram.get());
	}

	@Test
	public void getProgramInfo() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		ProgramDTO program = new ProgramDTO(
				pId,
				ExecutionState.SUCCESS,
				rnd.nextLong(),
				rnd.nextLong(),
				UUID.randomUUID().toString(),
				"name",
				"description",
				0,
				new HashMap<>(),
				new LinkedList<>()
		);
		when(requestServiceMock.getProgram(pId)).thenReturn(ticketId);
		when(responseServiceMock.getProgram(ticketId)).thenReturn(program);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<ProgramDTO> programInfo = client.getProgram(pId);
		assertEquals(program, programInfo.get());
	}

	@Test
	public void getAllPrograms() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String userId = UUID.randomUUID().toString();
		List<String> pIds = new LinkedList<>();
		pIds.add(UUID.randomUUID().toString());
		pIds.add(UUID.randomUUID().toString());
		when(requestServiceMock.getAllPrograms(userId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getAllPrograms(ticketId)).thenReturn(pIds);
		Future<List<String>> futurePrograms = client.getAllPrograms(userId);
		assertEquals(pIds, futurePrograms.get());
	}


	@Test
	public void deleteProgram() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		when(requestServiceMock.deleteProgram(pId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> deleteProgram = client.deleteProgram(pId);
		await().atMost(30, TimeUnit.SECONDS).until(deleteProgram::isDone);
	}

	@Test
	public void abortProgram() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		when(requestServiceMock.abortProgram(pId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> abortProgram = client.abortProgram(pId);
		await().atMost(30, TimeUnit.SECONDS).until(abortProgram::isDone);
	}

	@Test
	public void updateProgramName() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		when(requestServiceMock.updateProgramName((String)notNull(), (String)notNull())).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> future = client.updateProgramName(pId, "name");
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void updateProgramDescription() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		when(requestServiceMock.updateProgramDescription((String)notNull(), (String)notNull())).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> future = client.updateProgramDescription(pId, "description");
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void markProgramAsFinished() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		when(requestServiceMock.markProgramAsFinished(pId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> programFinished = client.markProgramAsFinished(pId);
		await().atMost(30, TimeUnit.SECONDS).until(programFinished::isDone);
	}

	@Test
	public void startClientRoutine() throws Exception {
		String pId = UUID.randomUUID().toString();
		String crId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		when(requestServiceMock.startClientRoutine(pId, crId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> attachAndStartClientRoutine = client.startClientRoutine(pId, crId);
		await().atMost(30, TimeUnit.SECONDS).until(attachAndStartClientRoutine::isDone);
		assertTrue(attachAndStartClientRoutine.isDone());
	}


	@Test
	public void getAllJobs() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		List<String> jobList = new LinkedList<>();
		int nrJobs = rnd.nextInt(10) + 1;
		for (int i = 0; i < nrJobs; i++) {
			jobList.add(Integer.toString(i));
		}
		when(requestServiceMock.getAllJobs(pId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getAllJobs(ticketId)).thenReturn(jobList);
		Future<List<String>> futureJobList = client.getAllJobs(pId);
		assertEquals(nrJobs, futureJobList.get().size());
		assertEquals(jobList, futureJobList.get());
	}

	@Test
	public void createJob() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		when(requestServiceMock.createJob(pId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.createJob(ticketId)).thenReturn(jId);
		Future<String> futureJob = client.createJob(pId);
		assertEquals(jId, futureJob.get());
	}

	@Test
	public void getJobInfo() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		JobDTO job = new JobDTO(
				jId, pId, ExecutionState.SUCCESS,
				rnd.nextLong(), rnd.nextLong(), rnd.nextLong(),
				rnd.nextInt(), rnd.nextInt(), rnd.nextInt(), rnd.nextInt(),
				UUID.randomUUID().toString(),
				new LinkedList<>()
		);
		when(requestServiceMock.getJob(pId, jId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getJob(ticketId)).thenReturn(job);
		Future<JobDTO> futureJob = client.getJob(pId, jId);
		assertEquals(job, futureJob.get());
	}

	@Test
	public void deleteJob() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		when(requestServiceMock.deleteJob(pId, jId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> jobDelete = client.deleteJob(pId, jId);
		await().atMost(30, TimeUnit.SECONDS).until(jobDelete::isDone);
	}

	@Test
	public void getAttachedMapRoutine() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();
		when(requestServiceMock.getAttachedMapRoutine(pId, jId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getAttachedMapRoutine(ticketId)).thenReturn(routineId);
		Future<String> mapRoutine = client.getAttachedMapRoutine(pId, jId);
		assertEquals(routineId, mapRoutine.get());
	}

	@Test
	public void attachMapRoutine() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();
		when(requestServiceMock.attachMapRoutine(pId, jId, routineId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> attachMapRoutine = client.attachMapRoutine(pId, jId, routineId);
		await().atMost(30, TimeUnit.SECONDS).until(attachMapRoutine::isDone);
	}


	@Test
	public void getAttachedReduceRoutine() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();
		when(requestServiceMock.getAttachedReduceRoutine(pId, jId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getAttachedReduceRoutine(ticketId)).thenReturn(routineId);
		Future<String> reduceRoutine = client.getAttachedReduceRoutine(pId, jId);
		assertEquals(routineId, reduceRoutine.get());
	}

	@Test
	public void attachReduceRoutine() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();
		when(requestServiceMock.attachReduceRoutine(pId, jId, routineId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> attachReduceRoutine = client.attachReduceRoutine(pId, jId, routineId);
		await().atMost(30, TimeUnit.SECONDS).until(attachReduceRoutine::isDone);
	}

	@Test
	public void getAllTasks() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		List<String> taskList = new LinkedList<>();
		int nrJobs = rnd.nextInt(10) + 1;
		for (int i = 0; i < nrJobs; i++) {
			taskList.add(Integer.toString(i));
		}
		when(requestServiceMock.getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.CREATION_DATE_FROM_NEWEST)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getAllTasksWithState(ticketId)).thenReturn(taskList);
		Future<List<String>> futureTaskList = client.getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.CREATION_DATE_FROM_NEWEST);
		assertEquals(nrJobs, futureTaskList.get().size());
		assertEquals(taskList, futureTaskList.get());
	}

	@Test
	public void createTask() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String taskId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();
		RoutineInstanceDTO routineInstance = new RoutineInstanceDTO();
		when(requestServiceMock.createTask(pId, jId, routineInstance)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.createTask(ticketId)).thenReturn(taskId);
		Future<String> task = client.createTask(pId, jId, routineInstance);
		assertEquals(taskId, task.get());
	}

	@Test
	public void getTask() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String taskId = UUID.randomUUID().toString();
		String objectiveRoutineId = UUID.randomUUID().toString();
		String mapRoutineId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO(
				taskId, jId, pId,
				ExecutionState.SUCCESS,
				rnd.nextLong(), rnd.nextLong(), rnd.nextLong(),
				objectiveRoutineId, mapRoutineId,
				new HashMap<>(), new LinkedList<>(), new LinkedList<>(), rnd.nextLong());
		when(requestServiceMock.getTask(pId, jId, taskId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getTask(ticketId)).thenReturn(task);
		Future<TaskDTO> futureTask = client.getTask(pId, jId, taskId);
		assertEquals(task, futureTask.get());
	}

	@Test
	public void getTaskPartial() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String taskId = UUID.randomUUID().toString();
		String objectiveRoutineId = UUID.randomUUID().toString();
		String mapRoutineId = UUID.randomUUID().toString();
		boolean includeInParameters = true;
		boolean includeOutParameters = false;
		TaskDTO task = new TaskDTO(
				taskId, jId, pId,
				ExecutionState.SUCCESS,
				rnd.nextLong(), rnd.nextLong(), rnd.nextLong(),
				objectiveRoutineId, mapRoutineId,
				new HashMap<>(), new LinkedList<>(), new LinkedList<>(), rnd.nextLong());
		when(requestServiceMock.getTaskPartial(pId, jId, taskId, includeInParameters, includeOutParameters)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getTaskPartial(ticketId)).thenReturn(task);
		Future<TaskDTO> futureTask = client.getTask(pId, jId, taskId, includeInParameters, includeOutParameters);
		assertEquals(task, futureTask.get());
	}

	@Test
	public void markJobAsComplete() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		when(requestServiceMock.markJobAsComplete(pId, jId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> jobComplete = client.markJobAsComplete(pId, jId);
		await().atMost(30, TimeUnit.SECONDS).until(jobComplete::isDone);
	}

	@Test
	public void abortJob() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		when(requestServiceMock.abortJob(pId, jId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> abortJob = client.abortJob(pId, jId);
		await().atMost(30, TimeUnit.SECONDS).until(abortJob::isDone);
	}

	@Test
	public void abortTask() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		when(requestServiceMock.abortTask(pId, jId, tId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> abortTask = client.abortTask(pId, jId, tId);
		await().atMost(30, TimeUnit.SECONDS).until(abortTask::isDone);
	}

	@Test
	public void reRunTask() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		when(requestServiceMock.reRunTask(pId, jId, tId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> abortTask = client.reRunTask(pId, jId, tId);
		await().atMost(30, TimeUnit.SECONDS).until(abortTask::isDone);
	}


	@Test
	public void getAllSharedResources() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		List<String> resources = new LinkedList<>();
		int nrOfResources = rnd.nextInt(10) + 1;
		for (int i = 0; i < nrOfResources; i++) {
			resources.add(Integer.toString(i));
		}
		when(requestServiceMock.getAllSharedResources(pId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getAllSharedResources(ticketId)).thenReturn(resources);
		Future<List<String>> futureResources = client.getAllSharedResources(pId);
		assertEquals(nrOfResources, futureResources.get().size());
		assertEquals(resources, futureResources.get());
	}

	@Test
	public void createSharedResource() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		String dataTypeId = UUID.randomUUID().toString();
		ByteBuffer data = ByteBuffer.wrap(new byte[] {0x00, 0x01, 0x02});
		when(requestServiceMock.createSharedResource(pId, dataTypeId, data)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.createSharedResource(ticketId)).thenReturn(rId);
		Future<String> sharedResource = client.createSharedResource(pId, dataTypeId, data);
		assertEquals(rId, sharedResource.get());
	}

	@Test
	public void getSharedResource() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		String dataTypeId = UUID.randomUUID().toString();
		ResourceDTO sharedResource = new ResourceDTO(rId, dataTypeId);
		when(requestServiceMock.getSharedResource(pId, rId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getSharedResource(ticketId)).thenReturn(sharedResource);
		Future<ResourceDTO> futureSharedResource = client.getSharedResource(pId, rId);
		assertEquals(sharedResource, futureSharedResource.get());
	}

	@Test
	public void deleteSharedResource() throws Exception {
		String pId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		when(requestServiceMock.deleteSharedResource(pId, rId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		Future<Void> deleteResource = client.deleteSharedResource(pId, rId);
		await().atMost(30, TimeUnit.SECONDS).until(deleteResource::isDone);
	}

	@Test
	public void getServiceEndpoint() throws Exception {
		ServiceEndpointDTO serviceEndpoint = client.getServiceEndpoint();
		assertNotNull(serviceEndpoint);
		assertEquals(requestClient.getServiceEndpoint(), serviceEndpoint);
		assertEquals(responseClient.getServiceEndpoint(), serviceEndpoint);
	}
}


