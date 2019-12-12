package at.enfilo.def.execlogic.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.communication.util.ServiceRegistry;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.transfer.dto.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class ExecLogicServiceTest {
	private IServer server;
	private Thread serverThread;
	private IExecLogicServiceClient execLogicClient;

	protected IExecLogicController execLogicController;

	@Before
	public void setUp() throws Exception {
		TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

		// Mocking internal services
		execLogicController = Mockito.mock(IExecLogicController.class);

		// Start service
		server = getServer();
		serverThread = new Thread(server);
		serverThread.start();

		await().atMost(10, TimeUnit.SECONDS).until(server::isRunning);
		ExecLogicServiceClientFactory factory = new ExecLogicServiceClientFactory();
		execLogicClient = factory.createClient(server.getServiceEndpoint());
	}

	protected abstract IServer getServer() throws Exception;

	@After
	public void tearDown() throws Exception {
		execLogicClient.close();
		server.close();
		serverThread.join();
		ServiceRegistry.getInstance().closeAll();
	}


	@Test
	public void getAllPrograms() throws Exception {
		String userId = UUID.randomUUID().toString();
		List<String> programIds = new LinkedList<>();
		programIds.add(UUID.randomUUID().toString());
		programIds.add(UUID.randomUUID().toString());

		when(execLogicController.getAllPrograms(userId)).thenReturn(programIds);

		Future<List<String>> futurePrograms = execLogicClient.getAllPrograms(userId);
		assertEquals(programIds, futurePrograms.get());
	}

	@Test
	public void createProgram() throws Exception {
		String cId = UUID.randomUUID().toString();
		String uId = UUID.randomUUID().toString();
		String pId = UUID.randomUUID().toString();

		when(execLogicController.createProgram(cId, uId)).thenReturn(pId);

		Future<String> futurePId = execLogicClient.createProgram(cId, uId);
		assertEquals(pId, futurePId.get());
	}


	@Test
	public void getProgramInfo() throws Exception {
		String pId = UUID.randomUUID().toString();
		ProgramDTO program = new ProgramDTO();
		program.setId(pId);

		when(execLogicController.getProgram(pId)).thenReturn(program);

		Future<ProgramDTO> futureProgram = execLogicClient.getProgram(pId);
		assertEquals(program, futureProgram.get());
	}

	@Test
	public void deleteProgram() throws Exception {
		String pId = UUID.randomUUID().toString();

		Future<Void> futureDeleteProgram = execLogicClient.deleteProgram(pId);
		await().atMost(10, TimeUnit.SECONDS).until(futureDeleteProgram::isDone);

		verify(execLogicController).deleteProgram(pId);
	}

	@Test
	public void abortProgram() throws Exception {
		String pId = UUID.randomUUID().toString();

		Future<Void> futureAbortProgram = execLogicClient.abortProgram(pId);
		await().atMost(10, TimeUnit.SECONDS).until(futureAbortProgram::isDone);

		verify(execLogicController).abortProgram(pId);
	}

	@Test
	public void updateProgramName() throws Exception {
		String name = "name";
		String pId = UUID.randomUUID().toString();

		Future<Void> future = execLogicClient.updateProgramName(pId, name);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(execLogicController).updateProgramName(pId, name);
	}

	@Test
	public void updateProgramDescription() throws Exception {
		String description = "description";
		String pId = UUID.randomUUID().toString();

		Future<Void> future = execLogicClient.updateProgramDescription(pId, description);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(execLogicController).updateProgramDescription(pId, description);
	}

	@Test
	public void markProgramAsFinished() throws Exception {
		String pId = UUID.randomUUID().toString();

		Future<Void> futureMarkProgram = execLogicClient.markProgramAsFinished(pId);
		await().atMost(10, TimeUnit.SECONDS).until(futureMarkProgram::isDone);

		verify(execLogicController).markProgramAsFinished(pId);
	}

	@Test
	public void getAllJobs() throws Exception {
		String pId = UUID.randomUUID().toString();
		List<String> jobIds = new LinkedList<>();
		jobIds.add(UUID.randomUUID().toString());
		jobIds.add(UUID.randomUUID().toString());

		when(execLogicController.getAllJobs(pId)).thenReturn(jobIds);

		Future<List<String>> futureJobs = execLogicClient.getAllJobs(pId);
		assertEquals(jobIds, futureJobs.get());
	}

	@Test
	public void createJob() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();

		when(execLogicController.createJob(pId)).thenReturn(jId);

		Future<String> futureJId = execLogicClient.createJob(pId);
		assertEquals(jId, futureJId.get());
	}

	@Test
	public void getJobInfo() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		JobDTO job = new JobDTO();
		job.setId(jId);

		when(execLogicController.getJob(pId, jId)).thenReturn(job);

		Future<JobDTO> futureJob = execLogicClient.getJob(pId, jId);
		assertEquals(job, futureJob.get());
	}

	@Test
	public void deleteJob() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();

		Future<Void> futureDeleteJob = execLogicClient.deleteJob(pId, jId);
		await().atMost(10, TimeUnit.SECONDS).until(futureDeleteJob::isDone);

		verify(execLogicController).deleteJob(pId, jId);
	}


	@Test
	public void getAttachedMapRoutine() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		when(execLogicController.getAttachedMapRoutine(pId, jId)).thenReturn(routineId);

		Future<String> futureMapRoutine = execLogicClient.getAttachedMapRoutine(pId, jId);
		assertEquals(routineId, futureMapRoutine.get());
	}

	@Test
	public void attachMapRoutine() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		Future<Void> futureMapRoutine = execLogicClient.attachMapRoutine(pId, jId, routineId);
		await().atMost(10, TimeUnit.SECONDS).until(futureMapRoutine::isDone);

		verify(execLogicController).attachMapRoutine(pId, jId, routineId);
	}

	@Test
	public void getAttachedReduceRoutine() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		when(execLogicController.getAttachedReduceRoutine(pId, jId)).thenReturn(routineId);

		Future<String> futureReduceRoutine = execLogicClient.getAttachedReduceRoutine(pId, jId);
		assertEquals(routineId, futureReduceRoutine.get());
	}

	@Test
	public void attachReduceRoutine() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		Future<Void> futureReduceRoutine = execLogicClient.attachReduceRoutine(pId, jId, routineId);
		await().atMost(10, TimeUnit.SECONDS).until(futureReduceRoutine::isDone);

		verify(execLogicController).attachReduceRoutine(pId, jId, routineId);
	}

	@Test
	public void getAllTasks() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		List<String> taskIds = new LinkedList<>();
		taskIds.add(UUID.randomUUID().toString());
		taskIds.add(UUID.randomUUID().toString());

		when(execLogicController.getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.NO_SORTING)).thenReturn(taskIds);

		Future<List<String>> futureTasks = execLogicClient.getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.NO_SORTING);
		assertEquals(taskIds, futureTasks.get());
	}

	@Test
	public void createTask() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		RoutineInstanceDTO routineInstance = new RoutineInstanceDTO();

		when(execLogicController.createTask(pId, jId, routineInstance)).thenReturn(tId);

		Future<String> futureTId = execLogicClient.createTask(pId, jId, routineInstance);
		assertEquals(tId, futureTId.get());
	}

	@Test
	public void getTask() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();
		task.setId(tId);

		when(execLogicController.getTask(pId, jId, tId)).thenReturn(task);

		Future<TaskDTO> futureTask = execLogicClient.getTask(pId, jId, tId);

		assertEquals(task, futureTask.get());
	}

	@Test
	public void getTaskPartial() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		boolean includeInParams = true;
		boolean includeOutParams = true;
		TaskDTO task = new TaskDTO();
		task.setId(tId);

		when(execLogicController.getTaskPartial(pId, jId, tId, includeInParams, includeOutParams)).thenReturn(task);

		Future<TaskDTO> futureTask = execLogicClient.getTask(pId, jId, tId, includeInParams, includeOutParams);

		assertEquals(task, futureTask.get());
	}

	@Test
	public void markJobAsComplete() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();

		Future<Void> futureMarkJob = execLogicClient.markJobAsComplete(pId, jId);
		await().atMost(10, TimeUnit.SECONDS).until(futureMarkJob::isDone);

		verify(execLogicController).markJobAsComplete(pId, jId);
	}

	@Test
	public void abortJob() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();

		Future<Void> futureAbortJob = execLogicClient.abortJob(pId, jId);
		await().atMost(10, TimeUnit.SECONDS).until(futureAbortJob::isDone);

		verify(execLogicController).abortJob(pId, jId);
	}

	@Test
	public void abortTask() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();

		Future<Void> futureAbortTask = execLogicClient.abortTask(pId, jId, tId);
		await().atMost(10, TimeUnit.SECONDS).until(futureAbortTask::isDone);

		verify(execLogicController).abortTask(pId, jId, tId);
	}

	@Test
	public void reRunTask() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();

		Future<Void> futureAbortTask = execLogicClient.reRunTask(pId, jId, tId);
		await().atMost(10, TimeUnit.SECONDS).until(futureAbortTask::isDone);

		verify(execLogicController).reRunTask(pId, jId, tId);
	}

	@Test
	public void getAllSharedResources() throws Exception {
		String pId = UUID.randomUUID().toString();
		List<String> resourceId = new LinkedList<>();
		resourceId.add(UUID.randomUUID().toString());
		resourceId.add(UUID.randomUUID().toString());

		when(execLogicController.getAllSharedResources(pId)).thenReturn(resourceId);

		Future<List<String>> futureTasks = execLogicClient.getAllSharedResources(pId);
		assertEquals(resourceId, futureTasks.get());
	}

	@Test
	public void createSharedResource() throws Exception {
		String pId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		String dataTypeId = UUID.randomUUID().toString();
		ByteBuffer data = ByteBuffer.wrap(new byte[] {0x00, 0x01, 0x02});

		when(execLogicController.createSharedResource(pId, dataTypeId, data)).thenReturn(rId);

		Future<String> futureResourceId = execLogicClient.createSharedResource(pId, dataTypeId, data);
		assertEquals(rId, futureResourceId.get());
	}

	@Test
	public void getSharedResource() throws Exception {
		String pId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		ResourceDTO resource = new ResourceDTO();
		resource.setId(rId);

		when(execLogicController.getSharedResource(pId, rId)).thenReturn(resource);

		Future<ResourceDTO> futureResource = execLogicClient.getSharedResource(pId, rId);
		assertEquals(resource, futureResource.get());
	}

}
