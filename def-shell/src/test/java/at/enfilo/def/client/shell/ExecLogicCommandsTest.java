package at.enfilo.def.client.shell;

import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.transfer.dto.*;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.shell.core.CommandResult;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import static at.enfilo.def.client.shell.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ExecLogicCommandsTest extends ShellBaseTest {

	@Test
	public void getAllPrograms() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String uId = UUID.randomUUID().toString();
		List<String> pIds = new LinkedList<>();
		String pId1 = UUID.randomUUID().toString();
		pIds.add(pId1);
		Future<List<String>> future = Mockito.mock(Future.class);
		when(clientMock.getAllPrograms(uId)).thenReturn(future);
		when(future.get()).thenReturn(pIds);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_EXEC_PROGRAM_LIST,
						OPT_USER_ID, uId
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(pId1));
	}

	@Test
	public void createProgram() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String uId = UUID.randomUUID().toString();
		String cId = UUID.randomUUID().toString();
		String pId = UUID.randomUUID().toString();
		Future<String> future = Mockito.mock(Future.class);
		when(clientMock.createProgram(cId, uId)).thenReturn(future);
		when(future.get()).thenReturn(pId);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_EXEC_PROGRAM_CREATE,
						OPT_USER_ID, uId,
						OPT_CLUSTER_ID, cId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_EXEC_PROGRAM_CREATED, pId, cId),
				result.getResult()
		);
	}


	@Test
	public void getProgram() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		ProgramDTO program = new ProgramDTO();
		program.setId(pId);
		Future<ProgramDTO> future = Mockito.mock(Future.class);
		when(clientMock.getProgram(pId)).thenReturn(future);
		when(future.get()).thenReturn(program);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_EXEC_PROGRAM_SHOW,
						OPT_PROGRAM_ID, pId
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(pId));
	}


	@Test
	public void getProgramToObject() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String objectName = UUID.randomUUID().toString();
		ProgramDTO program = new ProgramDTO();
		program.setId(pId);
		Future<ProgramDTO> future = Mockito.mock(Future.class);
		when(clientMock.getProgram(pId)).thenReturn(future);
		when(future.get()).thenReturn(program);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_EXEC_PROGRAM_SHOW,
						OPT_PROGRAM_ID, pId,
						OPT_TO_OBJECT, objectName
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(program, objects.getObject(objectName, ProgramDTO.class));
	}


	@Test
	public void deleteProgram() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.deleteProgram(pId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_EXEC_PROGRAM_REMOVE,
						OPT_PROGRAM_ID, pId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_EXEC_PROGRAM_REMOVE,
				result.getResult()
		);
	}


	@Test
	public void markProgramAsFinished() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.markProgramAsFinished(pId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_EXEC_PROGRAM_MARK_FINISHED,
						OPT_PROGRAM_ID, pId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_EXEC_PROGRAM_MARK_FINISHED,
				result.getResult()
		);
	}


	@Test
	public void getAllJobs() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		List<String> jobIds = new LinkedList<>();
		String jId1 = UUID.randomUUID().toString();
		jobIds.add(jId1);
		Future<List<String>> future = Mockito.mock(Future.class);
		when(clientMock.getAllJobs(pId)).thenReturn(future);
		when(future.get()).thenReturn(jobIds);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_EXEC_JOB_LIST,
						OPT_PROGRAM_ID, pId
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(jId1));
	}


	@Test
	public void createJob() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		Future<String> future = Mockito.mock(Future.class);
		when(clientMock.createJob(pId)).thenReturn(future);
		when(future.get()).thenReturn(jId);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_EXEC_JOB_CREATE,
						OPT_PROGRAM_ID, pId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_EXEC_JOB_CREATED, jId),
				result.getResult()
		);
	}


	@Test
	public void getJob() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		JobDTO job = new JobDTO();
		job.setId(jId);
		Future<JobDTO> future = Mockito.mock(Future.class);
		when(clientMock.getJob(pId, jId)).thenReturn(future);
		when(future.get()).thenReturn(job);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_EXEC_JOB_SHOW,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(jId));

	}

	@Test
	public void getJobToObject() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String objectName = UUID.randomUUID().toString();
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		JobDTO job = new JobDTO();
		job.setId(jId);
		Future<JobDTO> future = Mockito.mock(Future.class);
		when(clientMock.getJob(pId, jId)).thenReturn(future);
		when(future.get()).thenReturn(job);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s", CMD_EXEC_JOB_SHOW,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId,
						OPT_TO_OBJECT, objectName
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(job, objects.getObject(objectName, JobDTO.class));
	}


	@Test
	public void deleteJob() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.deleteJob(pId, jId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_EXEC_JOB_REMOVE,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_EXEC_JOB_REMOVE,
				result.getResult()
		);
	}


	@Test
	public void getAttachedMapRoutine() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		Future<String> future = Mockito.mock(Future.class);
		when(clientMock.getAttachedMapRoutine(pId, jId)).thenReturn(future);
		when(future.get()).thenReturn(rId);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_EXEC_JOB_SHOW_MAP_ROUTINE,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_EXEC_JOB_GET_MAP_ROUTINE, jId, rId),
				result.getResult()
		);
	}


	@Test
	public void attachMapRoutine() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.attachMapRoutine(pId, jId, rId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s", CMD_EXEC_JOB_ATTACH_MAP_ROUTINE,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId,
						OPT_ROUTINE_ID, rId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_EXEC_JOB_ATTACH_MAP_ROUTINE,
				result.getResult()
		);
	}


	@Test
	public void getAttachedReduceRoutine() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		Future<String> future = Mockito.mock(Future.class);
		when(clientMock.getAttachedReduceRoutine(pId, jId)).thenReturn(future);
		when(future.get()).thenReturn(rId);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_EXEC_JOB_SHOW_REDUCE_ROUTINE,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_EXEC_JOB_GET_REDUCE_ROUTINE, jId, rId),
				result.getResult()
		);
	}


	@Test
	public void attachReduceRoutine() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.attachReduceRoutine(pId, jId, rId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s", CMD_EXEC_JOB_ATTACH_REDUCE_ROUTINE,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId,
						OPT_ROUTINE_ID, rId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_EXEC_JOB_ATTACH_REDUCE_ROUTINE,
				result.getResult()
		);
	}


	@Test
	public void getAllTasks() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		List<String> taskIds = new LinkedList<>();
		String tId1 = UUID.randomUUID().toString();
		taskIds.add(tId1);
		Future<List<String>> future = Mockito.mock(Future.class);
		when(clientMock.getAllTasksWithState(pId, jId, ExecutionState.SCHEDULED, SortingCriterion.NO_SORTING)).thenReturn(future);
		when(future.get()).thenReturn(taskIds);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s", CMD_EXEC_TASK_LIST,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId,
						OPT_EXECUTION_STATE, ExecutionState.SCHEDULED

				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(tId1));

		result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s --%s %s", CMD_EXEC_TASK_LIST,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId,
						OPT_EXECUTION_STATE, ExecutionState.SCHEDULED,
						OPT_SORTING_CRITERION, SortingCriterion.NO_SORTING

				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(tId1));
	}


	@Test
	public void createTask() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		String routineInstanceName = UUID.randomUUID().toString();
		RoutineInstanceDTO routineInstance = new RoutineInstanceDTO();
		objects.getObjectMap().put(routineInstanceName, routineInstance);
		Future<String> future = Mockito.mock(Future.class);
		when(clientMock.createTask(pId, jId, routineInstance)).thenReturn(future);
		when(future.get()).thenReturn(tId);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s ", CMD_EXEC_TASK_CREATE,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId,
						OPT_ROUTINE_INSTANCE, routineInstanceName
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_EXEC_TASK_CREATED, tId),
				result.getResult()
		);
	}

	@Test
	public void getTask() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();
		task.setId(tId);
		Future<TaskDTO> future = Mockito.mock(Future.class);
		when(clientMock.getTask(pId, jId, tId)).thenReturn(future);
		when(future.get()).thenReturn(task);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s", CMD_EXEC_TASK_SHOW,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId,
						OPT_TASK_ID, tId
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(tId));

	}

	@Test
	public void getTaskToObject() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String objectName = UUID.randomUUID().toString();
		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();
		task.setId(jId);
		Future<TaskDTO> future = Mockito.mock(Future.class);
		when(clientMock.getTask(pId, jId, tId)).thenReturn(future);
		when(future.get()).thenReturn(task);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s --%s %s", CMD_EXEC_TASK_SHOW,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId,
						OPT_TASK_ID, tId,
						OPT_TO_OBJECT, objectName
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(task, objects.getObject(objectName, TaskDTO.class));
	}


	@Test
	public void markJobAsComplete() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.markJobAsComplete(pId, jId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_EXEC_JOB_MARK_COMPLETE,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_EXEC_JOB_MARK_COMPLETE,
				result.getResult()
		);
	}

	@Test
	public void reRunTask() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.reRunTask(pId, jId, tId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s", CMD_EXEC_TASK_RERUN,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId,
						OPT_TASK_ID, tId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_EXEC_TASK_RERUN,
				result.getResult()
		);
	}
	@Test
	public void abortTask() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.abortTask(pId, jId, tId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s", CMD_EXEC_TASK_ABORT,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId,
						OPT_TASK_ID, tId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_EXEC_TASK_ABORTED,
				result.getResult()
		);
	}

	@Test
	public void abortJob() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.abortJob(pId, jId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_EXEC_JOB_ABORT,
						OPT_PROGRAM_ID, pId,
						OPT_JOB_ID, jId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_EXEC_JOB_ABORTED,
				result.getResult()
		);
	}


	@Test
	public void getAllSharedResources() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		List<String> resourceIds = new LinkedList<>();
		String rId1 = UUID.randomUUID().toString();
		resourceIds.add(rId1);
		Future<List<String>> future = Mockito.mock(Future.class);
		when(clientMock.getAllSharedResources(pId)).thenReturn(future);
		when(future.get()).thenReturn(resourceIds);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_EXEC_SHARED_RESOURCE_LIST,
						OPT_PROGRAM_ID, pId
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(rId1));
	}


	@Test
	public void createSharedResource() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		String dataTypeId = UUID.randomUUID().toString();
		ByteBuffer data = ByteBuffer.wrap(new byte[] {0x00, 0x01, 0x02});
		Future<String> future = Mockito.mock(Future.class);
		when(clientMock.createSharedResource(pId, dataTypeId, data)).thenReturn(future);
		when(future.get()).thenReturn(rId);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s", CMD_EXEC_SHARED_RESOURCE_CREATE,
						OPT_PROGRAM_ID, pId,
						OPT_DATA_TYPE, dataTypeId,
						OPT_DATA, "000102"
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_EXEC_RESOURCE_CREATED, rId),
				result.getResult()
		);
	}

	@Test
	public void getSharedResource() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		ResourceDTO resource = new ResourceDTO();
		resource.setId(rId);
		Future<ResourceDTO> future = Mockito.mock(Future.class);
		when(clientMock.getSharedResource(pId, rId)).thenReturn(future);
		when(future.get()).thenReturn(resource);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_EXEC_SHARED_RESOURCE_SHOW,
						OPT_PROGRAM_ID, pId,
						OPT_RESOURCE_ID, rId
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(rId));

	}

	@Test
	public void getSharedResourceToObject() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String objectName = UUID.randomUUID().toString();
		String pId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		ResourceDTO resource = new ResourceDTO();
		resource.setId(rId);
		Future<ResourceDTO> future = Mockito.mock(Future.class);
		when(clientMock.getSharedResource(pId, rId)).thenReturn(future);
		when(future.get()).thenReturn(resource);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s", CMD_EXEC_SHARED_RESOURCE_SHOW,
						OPT_PROGRAM_ID, pId,
						OPT_RESOURCE_ID, rId,
						OPT_TO_OBJECT, objectName
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(resource, objects.getObject(objectName, ResourceDTO.class));
	}


	@Test
	public void deleteSharedResource() throws Exception {
		IExecLogicServiceClient clientMock = setupMocks();

		String pId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.deleteSharedResource(pId, rId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_EXEC_SHARED_RESOURCE_REMOVE,
						OPT_PROGRAM_ID, pId,
						OPT_RESOURCE_ID, rId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_EXEC_RESOURCE_REMOVE,
				result.getResult()
		);
	}


	private IExecLogicServiceClient setupMocks() throws ClientCreationException {
		changeToExecLogicContext();
		IExecLogicServiceClient clientMock = Mockito.mock(IExecLogicServiceClient.class);
		session.setExecLogicServiceClient(clientMock);
		return clientMock;
	}
}
