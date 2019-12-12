package at.enfilo.def.worker.impl;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.UnknownTaskException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;
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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class WorkerServiceControllerTest {

	private WorkerServiceController controller;
	private QueuePriorityWrapper<TaskDTO> queuePriorityWrapper;
	private List<INodeObserverServiceClient> observers;
	private WorkerServiceClientFactory workerServiceClientFactory;
	private Set<String> finishedTasks;
	private NodeObserverServiceClientFactory nodeObserverServiceClientFactory;

	@Before
	public void setUp() throws Exception {
		queuePriorityWrapper = new QueuePriorityWrapper<>(NodeConfiguration.getDefault());
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

		controller.queueElements(jId1, tasks1);
		controller.queueElements(jId2, tasks2);

		assertEquals(2, controller.getQueuedElements(jId1).size());
		assertEquals(2, controller.getQueueInfo(jId1).getNumberOfTasks());
		assertTrue(controller.getQueuedElements(jId1).contains(t11Id));
		assertTrue(controller.getQueuedElements(jId1).contains(t12Id));
		assertTrue(controller.getQueuedElements(jId2).contains(t2Id));
		assertFalse(controller.getQueuedElements(jId1).contains(t2Id));

		verify(observerClient).notifyTasksReceived(anyString(), eq(tasks1.stream().map(TaskDTO::getId).collect(Collectors.toList())));
		verify(observerClient).notifyTasksReceived(anyString(), eq(tasks2.stream().map(TaskDTO::getId).collect(Collectors.toList())));
		verify(observerClient, times(2)).notifyTasksReceived(anyString(), anyObject());
	}

	@Test
	public void pauseAndReleaseQueue() throws Exception {
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

		assertTrue(controller.getQueuedElements(jId).contains(t1Id));
		assertTrue(controller.getQueuedElements(jId).contains(t2Id));
		assertTrue(controller.getQueuedElements(jId).contains(t3Id));

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
		assertEquals(3, controller.getQueuePriorityWrapper().getNumberOfQueuedElements());

		// Move tasks
		controller.moveElements(jId, tIdsToMove, endpoint);
		assertFalse(controller.getQueuedElements(jId).contains(t1Id));
		assertTrue(controller.getQueuedElements(jId).contains(t2Id));
		assertFalse(controller.getQueuedElements(jId).contains(t3Id));

		verify(clientMock).queueTasks(jId, tasksToMove);

		// Verify observer
		assertEquals(1, controller.getQueuePriorityWrapper().getNumberOfQueuedElements());
		verify(observerClient).notifyNodeInfo(anyString(), anyObject());
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

		assertTrue(controller.getQueuedElements(jId1).contains(t11Id));
		assertTrue(controller.getQueuedElements(jId1).contains(t12Id));
		assertTrue(controller.getQueuedElements(jId2).contains(t21Id));
		assertTrue(controller.getQueuedElements(jId2).contains(t22Id));

		// Prepare destination endpoints and mocks
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		IWorkerServiceClient clientMock = Mockito.mock(IWorkerServiceClient.class);
		when(workerServiceClientFactory.createClient(endpoint)).thenReturn(clientMock);
		Future<Void> ticketMock = Mockito.mock(Future.class);
		when(clientMock.queueTasks(anyString(), anyList())).thenReturn(ticketMock);
		when(ticketMock.get()).thenReturn(null);

		controller.moveAllElements(endpoint);

		verify(clientMock, times(1)).queueTasks(eq(jId1), anyList());
		verify(clientMock, times(1)).queueTasks(eq(jId2), anyList());

		// Verify observer
		assertEquals(0, controller.getQueuePriorityWrapper().getNumberOfQueuedElements());
		verify(observerClient).notifyNodeInfo(anyString(), anyObject());
	}


	@Test(expected = UnknownTaskException.class)
	public void fetchFinishedUnknownTask() throws Exception {
		controller.fetchFinishedElement(UUID.randomUUID().toString());
	}

	@Test
	public void getSetStoreRoutine() throws Exception {
		assertEquals(Worker.getInstance().getConfiguration().getStoreRoutineId(), controller.getStoreRoutineId());

		String id = UUID.randomUUID().toString();
		controller.setStoreRoutineId(id);
		assertEquals(id, controller.getStoreRoutineId());
	}

	@Test
	public void getQueueIds() throws Exception {
		String queueId = UUID.randomUUID().toString();
		TaskQueue queue = new TaskQueue(queueId);
		queuePriorityWrapper.addQueue(queue);

		List<String> queueIds = controller.getQueueIds();

		assertEquals(1, queueIds.size());
		assertTrue(queueIds.contains(queueId));
	}

	@Test
	public void abortTask() throws Exception {
		TaskDTO task = new TaskDTO();
		String taskId = UUID.randomUUID().toString();
		task.setId(taskId);
		task.setState(ExecutionState.SCHEDULED);
		controller.getTaskCache().cache(taskId, task);

		controller.abortTask(taskId);

		assertTrue(task.getMessages().size() > 0);
	}
}


