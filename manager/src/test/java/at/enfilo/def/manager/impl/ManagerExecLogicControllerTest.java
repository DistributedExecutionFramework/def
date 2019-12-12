package at.enfilo.def.manager.impl;

import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.manager.util.ProgramClusterRegistry;
import at.enfilo.def.persistence.api.IPersistenceFacade;
import at.enfilo.def.transfer.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;

public class ManagerExecLogicControllerTest {
	private ManagerExecLogicController controller;
	private ProgramClusterRegistry registryMock;
	private ExecLogicServiceClientFactory factoryMock;
	private IExecLogicServiceClient clusterClientMock;
	private IPersistenceFacade persistenceMock;
	private String cId;
	private String pId;

	@Before
	public void setUp() throws Exception {
		cId = UUID.randomUUID().toString();
		pId = UUID.randomUUID().toString();

		clusterClientMock = Mockito.mock(IExecLogicServiceClient.class);
		registryMock = Mockito.mock(ProgramClusterRegistry.class);
		factoryMock = Mockito.mock(ExecLogicServiceClientFactory.class);
		persistenceMock = Mockito.mock(IPersistenceFacade.class);
		when(factoryMock.createClient(anyObject())).thenReturn(clusterClientMock);
		when(registryMock.isProgramRegistered(pId)).thenReturn(true);
		when(registryMock.getClusterId(pId)).thenReturn(cId);

		Constructor<ManagerExecLogicController> constructor = ManagerExecLogicController.class.getDeclaredConstructor(
				ExecLogicServiceClientFactory.class,
				ProgramClusterRegistry.class,
				IPersistenceFacade.class
		);
		constructor.setAccessible(true);

		controller = constructor.newInstance(factoryMock, registryMock, persistenceMock);

	}


	@Test
	public void createProgram() throws Exception {
		String uId = UUID.randomUUID().toString();
		Future<String> future = Mockito.mock(Future.class);
		when(clusterClientMock.createProgram(cId, uId)).thenReturn(future);
		when(future.get()).thenReturn(pId);

		String createdPId = controller.createProgram(cId, uId);
		assertEquals(pId, createdPId);
	}


	@Test
	public void getProgram() throws Exception {
		ProgramDTO program = new ProgramDTO();
		Future<ProgramDTO> future = Mockito.mock(Future.class);
		when(clusterClientMock.getProgram(pId)).thenReturn(future);
		when(future.get()).thenReturn(program);

		ProgramDTO requestedProgram = controller.getProgram(pId);
		assertEquals(program, requestedProgram);
	}


	@Test
	public void deleteProgram() throws Exception {
		Future<Void> future = Mockito.mock(Future.class);
		when(clusterClientMock.deleteProgram(pId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		controller.deleteProgram(pId);
	}

	@Test
	public void abortProgram() throws Exception {
		Future<Void> future = Mockito.mock(Future.class);
		when(clusterClientMock.abortProgram(pId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		controller.abortProgram(pId);
	}

	@Test
	public void updateProgramName() throws Exception {
		Future<Void> future = Mockito.mock(Future.class);
		when(clusterClientMock.updateProgramName((String)notNull(), (String)notNull())).thenReturn(future);
		when(future.get()).thenReturn(null);

		controller.updateProgramName(pId, "name");
	}

	@Test
	public void updateProgramDescription() throws Exception {
		Future<Void> future = Mockito.mock(Future.class);
		when(clusterClientMock.updateProgramDescription((String)notNull(), (String)notNull())).thenReturn(future);
		when(future.get()).thenReturn(null);

		controller.updateProgramDescription(pId, "description");
	}

	@Test
	public void markProgramAsFinished() throws Exception {
		Future<Void> future = Mockito.mock(Future.class);
		when(clusterClientMock.markProgramAsFinished(pId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		controller.markProgramAsFinished(pId);
	}


	@Test
	public void getAllJobs() throws Exception {
		List<String> jIds = new LinkedList<>();
		jIds.add(UUID.randomUUID().toString());
		jIds.add(UUID.randomUUID().toString());
		Future<List<String>> future = Mockito.mock(Future.class);
		when(clusterClientMock.getAllJobs(pId)).thenReturn(future);
		when(future.get()).thenReturn(jIds);

		List<String> requestedJIds = controller.getAllJobs(pId);
		assertEquals(jIds, requestedJIds);
	}

	@Test
	public void createJob() throws Exception {
		String jId = UUID.randomUUID().toString();
		Future<String> future = Mockito.mock(Future.class);
		when(clusterClientMock.createJob(pId)).thenReturn(future);
		when(future.get()).thenReturn(jId);

		String createdJId = controller.createJob(pId);
		assertEquals(jId, createdJId);
	}

	@Test
	public void getJob() throws Exception {
		JobDTO job = new JobDTO();
		String jId = UUID.randomUUID().toString();
		job.setId(jId);
		Future<JobDTO> future = Mockito.mock(Future.class);
		when(clusterClientMock.getJob(pId, jId)).thenReturn(future);
		when(future.get()).thenReturn(job);

		JobDTO requestedJob = controller.getJob(pId, jId);
		assertEquals(job, requestedJob);
	}

	@Test
	public void deleteJob() throws Exception {
		String jId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clusterClientMock.deleteJob(pId, jId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		controller.deleteJob(pId, jId);
	}

	@Test
	public void getAttachedMapRoutine() throws Exception {
		String rId =UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		Future<String> future = Mockito.mock(Future.class);
		when(clusterClientMock.getAttachedMapRoutine(pId, jId)).thenReturn(future);
		when(future.get()).thenReturn(rId);

		String requestedRId = controller.getAttachedMapRoutine(pId, jId);
		assertEquals(rId, requestedRId);
	}

	@Test
	public void attachMapRoutine() throws Exception {
		String jId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clusterClientMock.attachMapRoutine(pId, jId, rId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		controller.attachMapRoutine(pId, jId, rId);
	}

	@Test
	public void getAttachedReduceRoutine() throws Exception {
		String jId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		Future<String> future = Mockito.mock(Future.class);
		when(clusterClientMock.getAttachedReduceRoutine(pId, jId)).thenReturn(future);
		when(future.get()).thenReturn(rId);

		String requestedRId = controller.getAttachedReduceRoutine(pId, jId);
		assertEquals(rId, requestedRId);
	}

	@Test
	public void attachReduceRoutine() throws Exception {
		String jId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clusterClientMock.attachReduceRoutine(pId, jId, rId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		controller.attachReduceRoutine(pId, jId, rId);
	}

	@Test
	public void getAllTasks() throws Exception {
		String jId = UUID.randomUUID().toString();
		List<String> tIds = new LinkedList<>();
		tIds.add(UUID.randomUUID().toString());
		tIds.add(UUID.randomUUID().toString());
		Future<List<String>> future = Mockito.mock(Future.class);
		when(clusterClientMock.getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.NO_SORTING)).thenReturn(future);
		when(future.get()).thenReturn(tIds);

		List<String> requestedTIds = controller.getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.NO_SORTING);
		assertEquals(tIds, requestedTIds);
	}

	@Test
	public void getAllTasksWithState() throws Exception {
		String jId = UUID.randomUUID().toString();
		List<String> tIds = new LinkedList<>();
		tIds.add(UUID.randomUUID().toString());
		tIds.add(UUID.randomUUID().toString());
		Future<List<String>> future = Mockito.mock(Future.class);
		when(clusterClientMock.getAllTasksWithState((String) notNull(), (String)notNull(), (ExecutionState)notNull(), (SortingCriterion)notNull())).thenReturn(future);
		when(future.get()).thenReturn(tIds);

		List<String> requestedTIds = controller.getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.NO_SORTING);
		assertEquals(tIds, requestedTIds);
	}

	@Test
	public void createTask() throws Exception {
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		RoutineInstanceDTO objectiveRoutine = new RoutineInstanceDTO();
		Future<String> future = Mockito.mock(Future.class);
		when(clusterClientMock.createTask(pId, jId, objectiveRoutine)).thenReturn(future);
		when(future.get()).thenReturn(tId);

		String createdTId = controller.createTask(pId, jId, objectiveRoutine);
		assertEquals(tId, createdTId);
	}

	@Test
	public void getTask() throws Exception {
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();
		Future<TaskDTO> future = Mockito.mock(Future.class);
		when(clusterClientMock.getTask(pId, jId, tId)).thenReturn(future);
		when(future.get()).thenReturn(task);

		TaskDTO requestedTask = controller.getTask(pId, jId, tId);
		assertEquals(task, requestedTask);
	}

	@Test
	public void markJobAsComplete() throws Exception {
		String jId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clusterClientMock.markJobAsComplete(pId, jId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		controller.markJobAsComplete(pId, jId);
	}

	@Test
	public void abortJob() throws Exception {
		String jId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clusterClientMock.abortJob(pId, jId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		controller.abortJob(pId, jId);
	}

	@Test
	public void abortTask() throws Exception {
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clusterClientMock.abortTask(pId, jId, tId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		controller.abortTask(pId, jId, tId);
	}

	@Test
	public void getAllSharedResources() throws Exception {
		List<String> rIds = new LinkedList<>();
		rIds.add(UUID.randomUUID().toString());
		rIds.add(UUID.randomUUID().toString());
		Future<List<String>> future = Mockito.mock(Future.class);
		when(clusterClientMock.getAllSharedResources(pId)).thenReturn(future);
		when(future.get()).thenReturn(rIds);

		List<String> requestedRIds = controller.getAllSharedResources(pId);
		assertEquals(rIds, requestedRIds);
	}

	@Test
	public void createSharedResource() throws Exception {
		String rId = UUID.randomUUID().toString();
		String dataTypeId = UUID.randomUUID().toString();
		Random rnd = new Random();
		byte[] buffer = new byte[16];
		rnd.nextBytes(buffer);
		ByteBuffer data = ByteBuffer.wrap(buffer);
		Future<String> future = Mockito.mock(Future.class);
		when(clusterClientMock.createSharedResource(pId, dataTypeId, data)).thenReturn(future);
		when(future.get()).thenReturn(rId);

		String createdRId = controller.createSharedResource(pId, dataTypeId, data);
		assertEquals(rId, createdRId);
	}

	@Test
	public void getSharedResource() throws Exception {
		String rId = UUID.randomUUID().toString();
		ResourceDTO resource = new ResourceDTO();
		Future<ResourceDTO> future = Mockito.mock(Future.class);
		when(clusterClientMock.getSharedResource(pId, rId)).thenReturn(future);
		when(future.get()).thenReturn(resource);

		ResourceDTO requestedResource = controller.getSharedResource(pId, rId);
		assertEquals(resource, requestedResource);
	}

	@Test
	public void deleteSharedResource() throws Exception {
		String rId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clusterClientMock.deleteSharedResource(pId, rId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		controller.deleteSharedResource(pId, rId);
	}
}
