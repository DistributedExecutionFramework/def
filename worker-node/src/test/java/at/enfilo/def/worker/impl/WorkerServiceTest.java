package at.enfilo.def.worker.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class WorkerServiceTest {
	private IServer server;
	private Thread serverThread;
	private IWorkerServiceClient client;
	private WorkerServiceController controller;

	@Before
	public void setUp() throws Exception {
		TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

		// Mocking internal services
		controller = Mockito.mock(WorkerServiceController.class);

		// Start server
		server = getServer(controller);
		serverThread = new Thread(server);
		serverThread.start();

		await().atMost(30, TimeUnit.SECONDS).until(server::isRunning);
		WorkerServiceClientFactory factory = new WorkerServiceClientFactory();
		client = factory.createClient(server.getServiceEndpoint());
	}

	@After
	public void tearDown() throws Exception {
		client.close();
		server.close();
		serverThread.join();
	}

	protected abstract IServer getServer(WorkerServiceController controller) throws Exception;

	@Test
	public void queueTasks() throws Exception {
		String queueId = UUID.randomUUID().toString();
		List<TaskDTO> tasks = new LinkedList<>();
		tasks.add(new TaskDTO());
		tasks.add(new TaskDTO());

		Future<Void> future = client.queueTasks(queueId, tasks);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(controller).queueElements(queueId, tasks);
	}

	@Test
	public void getQueuedTasks() throws Exception {
		String queueId = UUID.randomUUID().toString();
		List<String> tasks = new LinkedList<>();
		tasks.add(UUID.randomUUID().toString());
		tasks.add(UUID.randomUUID().toString());

		when(controller.getQueuedElements(queueId)).thenReturn(tasks);

		Future<List<String>> future = client.getQueuedTasks(queueId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		List<String> requestedTasks = future.get();

		assertEquals(tasks, requestedTasks);
	}

	@Test
	public void moveTasks() throws Exception {
		String queueId = UUID.randomUUID().toString();
		List<String> tasks = new LinkedList<>();
		tasks.add(UUID.randomUUID().toString());
		tasks.add(UUID.randomUUID().toString());
		ServiceEndpointDTO worker = new ServiceEndpointDTO();

		Future<Void> future = client.moveTasks(queueId, tasks, worker);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(controller).moveElements(queueId, tasks, worker);
	}

	@Test
	public void moveAllTasks() throws Exception {
		ServiceEndpointDTO worker = new ServiceEndpointDTO();

		Future<Void> future = client.moveAllTasks(worker);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(controller).moveAllElements(worker);
	}

	@Test
	public void fetchFinishedTask() throws Exception {
		String taskId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();

		when(controller.fetchFinishedElement(taskId)).thenReturn(task);

		Future<TaskDTO> future = client.fetchFinishedTask(taskId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		TaskDTO requestedTask = future.get();

		assertEquals(task, requestedTask);
	}

	@Test
	public void abortTask() throws Exception {
		String tId = UUID.randomUUID().toString();

		Future<Void> future = client.abortTask(tId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
	}
}
