package at.enfilo.def.client.shell;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.PeriodUnit;
import at.enfilo.def.transfer.dto.QueueInfoDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.shell.core.CommandResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;

import static at.enfilo.def.client.shell.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WorkerCommandsTest extends ShellBaseTest {


	@Test
	public void takeControl() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		String clusterId = UUID.randomUUID().toString();
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.takeControl(clusterId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
			String.format("%s --%s %s", CMD_WORKER_TAKE_CONTROL,
					OPT_CLUSTER_ID, clusterId
			)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_WORKER_TAKE_CONTROL,
				result.getResult().toString()
		);
	}

	@Test
	public void getInfo() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		NodeInfoDTO info = new NodeInfoDTO();
		String id = UUID.randomUUID().toString();
		info.setId(id);
		Future<NodeInfoDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getInfo()).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(info);

		CommandResult result = shell.executeCommand(CMD_WORKER_SHOW);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(id));
	}

	@Test
	public void getInfoToObject() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		String name = UUID.randomUUID().toString();
		NodeInfoDTO info = new NodeInfoDTO();
		String id = UUID.randomUUID().toString();
		info.setId(id);
		Future<NodeInfoDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getInfo()).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(info);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_WORKER_SHOW,
						OPT_TO_OBJECT, name
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(info, objects.getObjectMap().get(name));
	}


	@Test
	public void registerObserver() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		String endpointName = UUID.randomUUID().toString();
		objects.getObjectMap().put(endpointName, endpoint);
		Boolean periodically = true;
		Integer periodDuration = new Random().nextInt();
		PeriodUnit periodUnit = PeriodUnit.MINUTES;

		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.registerObserver(endpoint, periodically, periodDuration, periodUnit)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);


		CommandResult result = shell.executeCommand(
			String.format("%s --%s %s --%s %b --%s %d --%s %s", CMD_WORKER_REGISTER_OBSERVER,
				OPT_SERVICE_ENDPOINT, endpointName,
				OPT_PERIODICALLY, periodically,
				OPT_PERIOD_DURATION, periodDuration,
				OPT_PERIOD_UNIT, periodUnit
			)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_WORKER_REGISTER_OBSERVER,
				result.getResult().toString()
		);
	}


	@Test
	public void deregisterObserver() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		String endpointName = UUID.randomUUID().toString();
		objects.getObjectMap().put(endpointName, endpoint);

		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.deregisterObserver(endpoint)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_WORKER_DEREGISTER_OBSERVER,
						OPT_SERVICE_ENDPOINT, endpointName
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_WORKER_DEREGISTER_OBSERVER,
				result.getResult().toString()
		);
	}


	@Test
	public void shutdown() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		CommandResult result = shell.executeCommand(CMD_WORKER_SHUTDOWN);
		assertTrue(result.isSuccess());
		assertEquals(MESSAGE_WORKER_SHUTDOWN, result.getResult().toString());

		verify(clientMock).shutdown();
	}


	@Test
	public void getQueues() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		List<String> queueIds = new LinkedList<>();
		String queueId = UUID.randomUUID().toString();
		queueIds.add(queueId);
		Future<List<String>> future = Mockito.mock(Future.class);
		when(clientMock.getQueues()).thenReturn(future);
		when(future.get()).thenReturn(queueIds);

		CommandResult result = shell.executeCommand(CMD_WORKER_QUEUE_LIST);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(queueId));
	}


	@Test
	public void createQueue() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		String jobId = UUID.randomUUID().toString();

		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.createQueue(jobId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_WORKER_QUEUE_CREATE, OPT_QUEUE_ID, jobId)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_WORKER_QUEUE_CREATED,
				result.getResult().toString()
		);
	}


	@Test
	public void getQueueInfo() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		String queueId = UUID.randomUUID().toString();
		QueueInfoDTO queueInfo = new QueueInfoDTO();
		queueInfo.setId(queueId);

		Future<QueueInfoDTO> futureInfo = Mockito.mock(Future.class);
		when(clientMock.getQueueInfo(queueId)).thenReturn(futureInfo);
		when(futureInfo.get()).thenReturn(queueInfo);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_WORKER_QUEUE_SHOW,
						OPT_QUEUE_ID, queueId
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(queueId));
	}


	@Test
	public void getQueueInfoToObject() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		String objectName = UUID.randomUUID().toString();
		String queueId = UUID.randomUUID().toString();
		QueueInfoDTO queueInfo = new QueueInfoDTO();
		queueInfo.setId(queueId);

		Future<QueueInfoDTO> futureInfo = Mockito.mock(Future.class);
		when(clientMock.getQueueInfo(queueId)).thenReturn(futureInfo);
		when(futureInfo.get()).thenReturn(queueInfo);

		CommandResult result = shell.executeCommand(
			String.format("%s --%s %s --%s %s", CMD_WORKER_QUEUE_SHOW,
				OPT_QUEUE_ID, queueId,
				OPT_TO_OBJECT, objectName
			)
		);
		assertTrue(result.isSuccess());
		assertEquals(queueInfo, objects.getObject(objectName, QueueInfoDTO.class));
	}



	@Test
	public void deleteQueue() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		String queueId = UUID.randomUUID().toString();
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.deleteQueue(queueId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_WORKER_QUEUE_REMOVE,
						OPT_QUEUE_ID, queueId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_WORKER_QUEUE_REMOVE,
				result.getResult().toString()
		);
	}


	@Test
	public void releaseQueue() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		String queueId = UUID.randomUUID().toString();
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.releaseQueue(queueId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_WORKER_QUEUE_RELEASE,
						OPT_QUEUE_ID, queueId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_WORKER_QUEUE_RELEASED,
				result.getResult().toString()
		);
	}

	@Test
	public void pauseQueue() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		String queueId = UUID.randomUUID().toString();
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.pauseQueue(queueId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_WORKER_QUEUE_PAUSE,
						OPT_QUEUE_ID, queueId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_WORKER_QUEUE_PAUSED,
				result.getResult().toString()
		);
	}

	@Test
	public void queueTasks() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		String queueId = UUID.randomUUID().toString();
		String t1Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		TaskDTO t1 = new TaskDTO();
		t1.setId(t1Id);
		TaskDTO t2 = new TaskDTO();
		t2.setId(t2Id);
		objects.getObjectMap().put(t1Id, t1);
		objects.getObjectMap().put(t2Id, t2);
		List<TaskDTO> tasks = new LinkedList<>();
		tasks.add(t1);
		tasks.add(t2);
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.queueTasks(queueId, tasks)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s,%s", CMD_WORKER_TASKS_QUEUE,
						OPT_QUEUE_ID, queueId,
						OPT_TASKS, t1Id, t2Id
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_WORKER_TASKS_QUEUE,
				result.getResult().toString()
		);
	}


	@Test
	public void getQueuedTasks() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		List<String> taskIds = new LinkedList<>();
		String queueId = UUID.randomUUID().toString();
		String tId = UUID.randomUUID().toString();
		taskIds.add(tId);
		Future<List<String>> future = Mockito.mock(Future.class);
		when(clientMock.getQueuedTasks(queueId)).thenReturn(future);
		when(future.get()).thenReturn(taskIds);

		CommandResult result = shell.executeCommand(
			String.format("%s --%s %s", CMD_WORKER_TASKS_LIST,
					OPT_QUEUE_ID, queueId
			)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(tId));
	}


	@Test
	public void moveTasks() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		String endpointName = UUID.randomUUID().toString();
		objects.getObjectMap().put(endpointName, endpoint);

		String queueId = UUID.randomUUID().toString();
		String t1Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		List<String> tasks = new LinkedList<>();
		tasks.add(t1Id);
		tasks.add(t2Id);
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.moveTasks(queueId, tasks, endpoint)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s,%s --%s %s", CMD_WORKER_TASKS_MOVE,
						OPT_QUEUE_ID, queueId,
						OPT_TASK_IDS, t1Id, t2Id,
						OPT_SERVICE_ENDPOINT, endpointName
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_WORKER_TASKS_MOVE,
				result.getResult().toString()
		);
	}


	@Test
	public void moveAllTasks() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		String endpointName = UUID.randomUUID().toString();
		objects.getObjectMap().put(endpointName, endpoint);
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.moveAllTasks(endpoint)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_WORKER_TASKS_MOVE_ALL,
						OPT_SERVICE_ENDPOINT, endpointName
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_WORKER_TASKS_MOVE,
				result.getResult().toString()
		);
	}


	@Test
	public void getFinishedTask() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		String queueId = UUID.randomUUID().toString();
		String taskId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();
		task.setId(taskId);

		Future<TaskDTO> futureTask = Mockito.mock(Future.class);
		when(clientMock.fetchFinishedTask(taskId)).thenReturn(futureTask);
		when(futureTask.get()).thenReturn(task);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_WORKER_TASKS_FETCH_FINISHED,
						OPT_QUEUE_ID, queueId,
						OPT_TASK_ID, taskId
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(taskId));
	}

	@Test
	public void getFinishedTaskToObject() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		String queueId = UUID.randomUUID().toString();
		String taskId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();
		task.setId(taskId);

		Future<TaskDTO> futureTask = Mockito.mock(Future.class);
		when(clientMock.fetchFinishedTask(taskId)).thenReturn(futureTask);
		when(futureTask.get()).thenReturn(task);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s", CMD_WORKER_TASKS_FETCH_FINISHED,
						OPT_QUEUE_ID, queueId,
						OPT_TASK_ID, taskId,
						OPT_TO_OBJECT, taskId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(task, objects.getObject(taskId, TaskDTO.class));
	}

	@Test
	public void setStoreRoutineId() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		String routineId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.setStoreRoutine(routineId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_WORKER_STORE_ROUTINE_SET,
						OPT_ROUTINE_ID, routineId
				)
		);
		assertTrue(result.isSuccess());
	}

	@Test
	public void getStoreRoutineId() throws Exception {
		IWorkerServiceClient clientMock = setupMocks();

		String routineId = UUID.randomUUID().toString();
		Future<String> future = Mockito.mock(Future.class);
		when(clientMock.getStoreRoutine()).thenReturn(future);
		when(future.get()).thenReturn(routineId);

		CommandResult result = shell.executeCommand(
				String.format("%s", CMD_WORKER_STORE_ROUTINE_SHOW)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(routineId));
	}

	private IWorkerServiceClient setupMocks() throws ClientCreationException {
		changeToWorkerContext();
		IWorkerServiceClient clientMock = Mockito.mock(IWorkerServiceClient.class);
		session.setWorkerServiceClient(clientMock);
		return clientMock;
	}
}
