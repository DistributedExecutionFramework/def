package at.enfilo.def.worker.impl;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.QueueNotExistsException;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.transfer.UnknownTaskException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;
import at.enfilo.def.worker.queue.QueuePriorityWrapper;
import at.enfilo.def.worker.queue.TaskQueue;
import at.enfilo.def.worker.server.Worker;
import at.enfilo.def.worker.util.WorkerConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class WorkerServiceControllerTest {

	private WorkerServiceController controller;
	private QueuePriorityWrapper queuePriorityWrapper;
	private List<INodeObserverServiceClient> observers;
	private WorkerServiceClientFactory workerServiceClientFactory;
	private Set<String> finishedTasks;
	private NodeObserverServiceClientFactory nodeObserverServiceClientFactory;

	@Before
	public void setUp() throws Exception {
		queuePriorityWrapper = new QueuePriorityWrapper();
		finishedTasks = new HashSet<>();
		observers = new LinkedList<>();
		workerServiceClientFactory = Mockito.mock(WorkerServiceClientFactory.class);
		nodeObserverServiceClientFactory = Mockito.mock(NodeObserverServiceClientFactory.class);

		Constructor<WorkerServiceController> constructor = WorkerServiceController.class.getDeclaredConstructor(
			QueuePriorityWrapper.class,
			Set.class,
			List.class,
			WorkerServiceClientFactory.class,
			WorkerConfiguration.class,
			NodeObserverServiceClientFactory.class,
			IDEFLogger.class
		);
		constructor.setAccessible(true);
		controller = constructor.newInstance(
			queuePriorityWrapper,
			finishedTasks,
			observers,
			workerServiceClientFactory,
			Worker.getInstance().getConfiguration(),
				nodeObserverServiceClientFactory,
			DEFLoggerFactory.getLogger(this.getClass())
		);
	}

	@Test
	public void createQueue() throws Exception {
		assertTrue(controller.getQueues().isEmpty());

		// Create first queue
		String pId = UUID.randomUUID().toString();
		String jId1 = UUID.randomUUID().toString();
		JobDTO j1 = new JobDTO(jId1, pId, ExecutionState.SCHEDULED, 0, 0, 0, 0, 0, 0, 0, "");
		controller.createQueue(jId1);
		assertEquals(1, controller.getQueues().size());
		assertTrue(controller.getQueues().contains(jId1));

		// Create second queue
		String jId2 = UUID.randomUUID().toString();
		JobDTO j2 = new JobDTO(jId2, pId, ExecutionState.SCHEDULED, 0, 0, 0, 0, 0, 0, 0, "");
		controller.createQueue(jId2);
		assertEquals(2, controller.getQueues().size());
		assertTrue(controller.getQueues().contains(jId2));

		// Create second queue again
		controller.createQueue(jId2);
		assertEquals(2, controller.getQueues().size());

		try {
			controller.getQueuedTasks(UUID.randomUUID().toString());
			fail();
		} catch (QueueNotExistsException e) {
			// Expected.
		}
	}

	@Test
	public void deleteQueue() throws Exception {
		assertTrue(controller.getQueues().isEmpty());

		String pId = UUID.randomUUID().toString();
		String jId1 = UUID.randomUUID().toString();
		String jId2 = UUID.randomUUID().toString();
		String jId3 = UUID.randomUUID().toString();
		TaskQueue tq1 = new TaskQueue(jId1);
		queuePriorityWrapper.addQueue(tq1);
		TaskQueue tq2 = new TaskQueue(jId2);
		queuePriorityWrapper.addQueue(tq2);
		TaskQueue tq3 = new TaskQueue(jId3);
		queuePriorityWrapper.addQueue(tq3);

		assertEquals(3, controller.getQueues().size());

		controller.deleteQueue(jId2);
		assertEquals(2, controller.getQueues().size());
		assertFalse(controller.getQueues().contains(jId2));
	}


	@Test
	public void queueTask() throws Exception {
		// Prepare observers
		INodeObserverServiceClient observerClient = Mockito.mock(INodeObserverServiceClient.class);
		observers.add(observerClient);

		// Prepare program and two jobs
		String pId = UUID.randomUUID().toString();
		String jId1 = UUID.randomUUID().toString();
		TaskQueue tq1 = new TaskQueue(jId1);
		queuePriorityWrapper.addQueue(tq1);

		String jId2 = UUID.randomUUID().toString();
		TaskQueue tq2 = new TaskQueue(jId2);
		queuePriorityWrapper.addQueue(tq2);

		Random rnd = new Random();
		String t11Id = UUID.randomUUID().toString();
		String t12Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		List<TaskDTO> tasks1 = new LinkedList<>();
		List<TaskDTO> tasks2 = new LinkedList<>();
		TaskDTO t11 = new TaskDTO(t11Id, jId1, pId, ExecutionState.FAILED, rnd.nextInt(), rnd.nextInt(),  rnd.nextInt(), null, null, null, null, null, rnd.nextLong());
		TaskDTO t12 = new TaskDTO(t12Id, jId1, pId, ExecutionState.FAILED, rnd.nextInt(), rnd.nextInt(), rnd.nextInt(), null, null, null, null, null, rnd.nextLong());
		tasks1.add(t11);
		tasks1.add(t12);
		TaskDTO t2 = new TaskDTO(t2Id, jId2, pId, ExecutionState.FAILED, rnd.nextInt(), rnd.nextInt(), rnd.nextInt(), null, null, null, null, null, rnd.nextLong());
		tasks2.add(t2);

		controller.queueTasks(jId1, tasks1);
		controller.queueTasks(jId2, tasks2);

		assertEquals(2, controller.getQueuedTasks(jId1).size());
		assertEquals(2, controller.getQueueInfo(jId1).getNumberOfTasks());
		assertTrue(controller.getQueuedTasks(jId1).contains(t11Id));
		assertTrue(controller.getQueuedTasks(jId1).contains(t12Id));
		assertTrue(controller.getQueuedTasks(jId2).contains(t2Id));
		assertFalse(controller.getQueuedTasks(jId1).contains(t2Id));

		verify(observerClient).notifyTasksReceived(anyString(), eq(tasks1.stream().map(TaskDTO::getId).collect(Collectors.toList())));
		verify(observerClient).notifyTasksReceived(anyString(), eq(tasks2.stream().map(TaskDTO::getId).collect(Collectors.toList())));
		verify(observerClient, times(2)).notifyTasksReceived(anyString(), anyObject());
	}

	@Test(expected = QueueNotExistsException.class)
	public void queueTaskUnknownQueue() throws Exception {
		List<TaskDTO> tasks = new LinkedList<>();
		tasks.add(new TaskDTO());
		controller.queueTasks(UUID.randomUUID().toString(), tasks);
	}

	@Test
	public void pauseAndReleaseQueue() throws Exception {
		String pId = UUID.randomUUID().toString();
		String jId1 = UUID.randomUUID().toString();
		TaskQueue tq1 = new TaskQueue(jId1);
		queuePriorityWrapper.addQueue(tq1);
		String jId2 = UUID.randomUUID().toString();
		TaskQueue tq2 = new TaskQueue(jId2);
		queuePriorityWrapper.addQueue(tq2);

		assertFalse(controller.getQueueInfo(jId1).isReleased());
		assertFalse(controller.getQueueInfo(jId2).isReleased());

		controller.releaseQueue(jId1);
		assertTrue(controller.getQueueInfo(jId1).isReleased());
		assertFalse(controller.getQueueInfo(jId2).isReleased());

		controller.pauseQueue(jId1);
		controller.pauseQueue(jId2);
		assertFalse(controller.getQueueInfo(jId1).isReleased());
		assertFalse(controller.getQueueInfo(jId2).isReleased());
	}

	@Test(expected = QueueNotExistsException.class)
	public void releaseUnknownQueue() throws Exception {
		controller.releaseQueue(UUID.randomUUID().toString());
	}

	@Test(expected = QueueNotExistsException.class)
	public void pauseUnknownQueue() throws Exception {
		controller.pauseQueue(UUID.randomUUID().toString());
	}

	@Test
	public void moveTasks() throws Exception {
		// Prepare observers
		INodeObserverServiceClient observerClient = Mockito.mock(INodeObserverServiceClient.class);
		observers.add(observerClient);

		// Prepare a job/queue with 3 tasks
		String jId = UUID.randomUUID().toString();
		JobDTO job = new JobDTO();
		job.setId(jId);

		String t1Id = UUID.randomUUID().toString();
		TaskDTO t1 = new TaskDTO();
		t1.setId(t1Id);
		String t2Id = UUID.randomUUID().toString();
		TaskDTO t2 = new TaskDTO();
		t2.setId(t2Id);
		String t3Id = UUID.randomUUID().toString();
		TaskDTO t3 = new TaskDTO();
		t3.setId(t3Id);

		TaskQueue tq = new TaskQueue(jId);
		queuePriorityWrapper.addQueue(tq);
		tq.queue(t1);
		tq.queue(t2);
		tq.queue(t3);

		assertTrue(controller.getQueuedTasks(jId).contains(t1Id));
		assertTrue(controller.getQueuedTasks(jId).contains(t2Id));
		assertTrue(controller.getQueuedTasks(jId).contains(t3Id));

		// Prepare destination endpoints and mocks
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		IWorkerServiceClient clientMock = Mockito.mock(IWorkerServiceClient.class);
		when(workerServiceClientFactory.createClient(endpoint)).thenReturn(clientMock);
		Future<Void> ticketMock = Mockito.mock(Future.class);

		List<TaskDTO> tasksToMove = new LinkedList<>();
		tasksToMove.add(t1);
		tasksToMove.add(t3);
		List<String> tIdsToMove = new LinkedList<>();
		tIdsToMove.add(t1Id);
		tIdsToMove.add(t3Id);

		when(clientMock.queueTasks(jId, tasksToMove)).thenReturn(ticketMock);
		when(ticketMock.get()).thenReturn(null);

		// Verify number of tasks
		assertEquals(3, controller.getNumberOfQueuedTasks());

		// Move tasks
		controller.moveTasks(jId, tIdsToMove, endpoint);
		assertFalse(controller.getQueuedTasks(jId).contains(t1Id));
		assertTrue(controller.getQueuedTasks(jId).contains(t2Id));
		assertFalse(controller.getQueuedTasks(jId).contains(t3Id));

		verify(clientMock).queueTasks(jId, tasksToMove);

		// Verify observer
		assertEquals(1, controller.getNumberOfQueuedTasks());
		verify(observerClient).notifyNodeInfo(anyString(), anyObject());
	}

	@Test(expected = QueueNotExistsException.class)
	public void moveTasksUnknownQueue() throws Exception {
		List<String> tIdsToMove = new LinkedList<>();
		tIdsToMove.add(UUID.randomUUID().toString());
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

		controller.moveTasks(UUID.randomUUID().toString(), tIdsToMove, endpoint);
	}

	@Test
	public void moveAllTasks() throws Exception {
		// Prepare observers
		INodeObserverServiceClient observerClient = Mockito.mock(INodeObserverServiceClient.class);
		observers.add(observerClient);

		// Prepare two jobs/queues with each two tasks
		String jId1 = UUID.randomUUID().toString();
		JobDTO j1 = new JobDTO();
		j1.setId(jId1);
		String jId2 = UUID.randomUUID().toString();
		JobDTO j2 = new JobDTO();
		j2.setId(jId2);

		String t11Id = UUID.randomUUID().toString();
		TaskDTO t11 = new TaskDTO();
		t11.setId(t11Id);
		String t12Id = UUID.randomUUID().toString();
		TaskDTO t12 = new TaskDTO();
		t12.setId(t12Id);
		String t21Id = UUID.randomUUID().toString();
		TaskDTO t21 = new TaskDTO();
		t21.setId(t21Id);
		String t22Id = UUID.randomUUID().toString();
		TaskDTO t22 = new TaskDTO();
		t22.setId(t22Id);

		TaskQueue tq1 = new TaskQueue(jId1);
		TaskQueue tq2 = new TaskQueue(jId2);
		queuePriorityWrapper.addQueue(tq1);
		queuePriorityWrapper.addQueue(tq2);
		tq1.queue(t11);
		tq1.queue(t12);
		tq2.queue(t21);
		tq2.queue(t22);

		assertTrue(controller.getQueuedTasks(jId1).contains(t11Id));
		assertTrue(controller.getQueuedTasks(jId1).contains(t12Id));
		assertTrue(controller.getQueuedTasks(jId2).contains(t21Id));
		assertTrue(controller.getQueuedTasks(jId2).contains(t22Id));

		// Prepare destination endpoints and mocks
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		IWorkerServiceClient clientMock = Mockito.mock(IWorkerServiceClient.class);
		when(workerServiceClientFactory.createClient(endpoint)).thenReturn(clientMock);
		Future<Void> ticketMock = Mockito.mock(Future.class);
		when(clientMock.queueTasks(anyString(), anyList())).thenReturn(ticketMock);
		when(ticketMock.get()).thenReturn(null);

		controller.moveAllTasks(endpoint);

		verify(clientMock, times(1)).queueTasks(eq(jId1), anyList());
		verify(clientMock, times(1)).queueTasks(eq(jId2), anyList());

		// Verify observer
		assertEquals(0, controller.getNumberOfQueuedTasks());
		verify(observerClient).notifyNodeInfo(anyString(), anyObject());
	}


	@Test(expected = UnknownTaskException.class)
	public void fetchFinishedUnknownTask() throws Exception {
		controller.fetchFinishedTask(UUID.randomUUID().toString());
	}

	@Test
	public void fetchFinishedTask() throws Exception {
		// Prepare finished tasks
		String t1Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		String t3Id = UUID.randomUUID().toString();
		TaskDTO t1 = new TaskDTO();
		t1.setId(t1Id);
		TaskDTO t2 = new TaskDTO();
		t2.setId(t2Id);
		TaskDTO t3 = new TaskDTO();
		t3.setId(t3Id);

		DTOCache<TaskDTO> cache =  DTOCache.getInstance(WorkerServiceController.DTO_TASK_CACHE_CONTEXT, TaskDTO.class);
		finishedTasks.add(t1Id);
		cache.cache(t1.getId(), t1);
		finishedTasks.add(t2Id);
		cache.cache(t2.getId(), t2);
		finishedTasks.add(t3Id);
		cache.cache(t3.getId(), t3);

		TaskDTO t1Finished = controller.fetchFinishedTask(t1Id);
		assertEquals(t1, t1Finished);
		TaskDTO t3Finished = controller.fetchFinishedTask(t3Id);
		assertEquals(t3, t3Finished);

		assertEquals(1, finishedTasks.size());
	}


	@Test
	public void notifyTaskSuccess() throws Exception {
		// Prepare task
		String t1Id = UUID.randomUUID().toString();
		TaskDTO t1 = new TaskDTO();
		t1.setId(t1Id);
		List<String> notifyTaskIds = new LinkedList<>();
		notifyTaskIds.add(t1Id);

		// Prepare observers
		INodeObserverServiceClient observer = Mockito.mock(INodeObserverServiceClient.class);
		observers.add(observer);

		assertTrue(finishedTasks.isEmpty());
		controller.notifyStateChanged(t1Id, ExecutionState.RUN, ExecutionState.SUCCESS);

		assertTrue(finishedTasks.contains(t1Id));
		verify(observer).notifyTasksNewState(anyString(), eq(notifyTaskIds), eq(ExecutionState.SUCCESS));
	}

	@Test
	public void notifyTaskFailed() throws Exception {
		// Prepare task
		String t1Id = UUID.randomUUID().toString();
		TaskDTO t1 = new TaskDTO();
		t1.setId(t1Id);
		List<String> notifyTaskIds = new LinkedList<>();
		notifyTaskIds.add(t1Id);

		// Prepare observers
		INodeObserverServiceClient observer = Mockito.mock(INodeObserverServiceClient.class);
		observers.add(observer);

		assertTrue(finishedTasks.isEmpty());
		controller.notifyStateChanged(t1Id, ExecutionState.RUN, ExecutionState.FAILED);

		assertTrue(finishedTasks.contains(t1Id));
		verify(observer).notifyTasksNewState(anyString(), eq(notifyTaskIds), eq(ExecutionState.FAILED));
	}

	@Test
	public void notifyTaskRun() throws Exception {
		// Prepare task
		String t1Id = UUID.randomUUID().toString();
		TaskDTO t1 = new TaskDTO();
		t1.setId(t1Id);
		List<String> notifyTaskIds = new LinkedList<>();
		notifyTaskIds.add(t1Id);

		// Prepare observers
		INodeObserverServiceClient observer = Mockito.mock(INodeObserverServiceClient.class);
		observers.add(observer);

		controller.notifyStateChanged(t1Id, ExecutionState.SCHEDULED, ExecutionState.RUN);

		verify(observer).notifyTasksNewState(anyString(), eq(notifyTaskIds), eq(ExecutionState.RUN));
	}

	@Test
	public void getSetStoreRoutine() throws Exception {
		assertEquals(Worker.getInstance().getConfiguration().getStoreRoutineId(), controller.getStoreRoutineId());

		String id = UUID.randomUUID().toString();
		controller.setStoreRoutineId(id);
		assertEquals(id, controller.getStoreRoutineId());
	}
}


