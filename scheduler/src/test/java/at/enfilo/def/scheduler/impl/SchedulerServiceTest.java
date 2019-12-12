package at.enfilo.def.scheduler.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.scheduler.api.ISchedulerServiceClient;
import at.enfilo.def.scheduler.api.SchedulerServiceClientFactory;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class SchedulerServiceTest {

	private ISchedulerServiceClient client;
	private IServer server;
	private Thread serverThread;

	protected SchedulingController schedulingController;

	@Before
	public void setUp() throws Exception {
		TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

		// Mocking controller
		schedulingController = Mockito.mock(SchedulingController.class);

		// Start server
		server = getServer();
		serverThread = new Thread(server);
		serverThread.start();

		// Start client
		await().atMost(10, TimeUnit.SECONDS).until(server::isRunning);
		SchedulerServiceClientFactory factory = new SchedulerServiceClientFactory();
		client = factory.createClient(server.getServiceEndpoint());
	}

	@After
	public void tearDown() throws Exception {
		client.close();
		server.close();
		serverThread.join();
	}

	/**
	 * Get real server.
	 * @return service instance
	 * @throws Exception
	 */
	protected abstract IServer getServer() throws Exception;

	@Test
	public void addJob() throws Exception {
		String jId = UUID.randomUUID().toString();

		Future<Void> futureAddJob = client.addJob(jId);

		await().atMost(10, TimeUnit.SECONDS).until(futureAddJob::isDone);
		assertNull(futureAddJob.get());
		verify(schedulingController).addJob(jId);
	}

	@Test
	public void removeJob() throws Exception {
		String jId = UUID.randomUUID().toString();

		Future<Void> future = client.removeJob(jId);

		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
		verify(schedulingController).removeJob(jId);
	}

	@Test
	public void scheduleTask() throws Exception {
		String jId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();

		Future<Void> futureAddJob = client.scheduleTask(jId, task);

		await().atMost(10, TimeUnit.SECONDS).until(futureAddJob::isDone);
		assertNull(futureAddJob.get());
		verify(schedulingController).scheduleTask(jId, Collections.singletonList(task));
	}


	@Test
	public void markJobAsComplete() throws Exception {
		String jId = UUID.randomUUID().toString();

		Future<Void> futureMarkJobAsComplete = client.markJobAsComplete(jId);

		await().atMost(10, TimeUnit.SECONDS).until(futureMarkJobAsComplete::isDone);
		assertNull(futureMarkJobAsComplete.get());
		verify(schedulingController).markJobAsComplete(jId);
	}

	@Test
	public void addWorker() throws Exception {
		String wId = UUID.randomUUID().toString();
		ServiceEndpointDTO serviceEndpoint = new ServiceEndpointDTO();

		Future<Void> future = client.addWorker(wId, serviceEndpoint);

		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
		verify(schedulingController).addWorker(wId, serviceEndpoint);
	}

	@Test
	public void removeWorker() throws Exception {
		String wId = UUID.randomUUID().toString();

		Future<Void> future = client.removeWorker(wId);

		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
		verify(schedulingController).removeWorker(wId);
	}

	@Test
	public void addReducer() throws Exception {
		String nId = UUID.randomUUID().toString();
		ServiceEndpointDTO serviceEndpoint = new ServiceEndpointDTO();

		Future<Void> future = client.addReducer(nId, serviceEndpoint);

		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
		verify(schedulingController).addReducer(nId, serviceEndpoint);
	}

	@Test
	public void removeReducer() throws Exception {
		String wId = UUID.randomUUID().toString();

		Future<Void> future = client.removeWorker(wId);

		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
		verify(schedulingController).removeWorker(wId);
	}

	@Test
	public void extendToReduceJob() throws Exception {
		String jId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();

		Future<Void> futureExtendJob = client.extendToReduceJob(jId, rId);

		await().atMost(10, TimeUnit.SECONDS).until(futureExtendJob::isDone);
		assertNull(futureExtendJob.get());
		verify(schedulingController).extendToReduceJob(jId, rId);
	}

	@Test
	public void scheduleResource() throws Exception {
		String jId = UUID.randomUUID().toString();
		List<ResourceDTO> resources = new LinkedList<>();
		resources.add(new ResourceDTO());
		resources.add(new ResourceDTO());

		Future<Void> futureScheduleReduce = client.scheduleResource(jId, resources);

		await().atMost(10, TimeUnit.SECONDS).until(futureScheduleReduce::isDone);
		assertNull(futureScheduleReduce.get());
		verify(schedulingController).scheduleReduce(jId, resources);
	}

	@Test
	public void finalizeReduce() throws Exception {
		String jId = UUID.randomUUID().toString();
		List<ResourceDTO> resources = new LinkedList<>();
		resources.add(new ResourceDTO());
		resources.add(new ResourceDTO());

		when(schedulingController.finalizeReduce(jId)).thenReturn(resources);

		Future<List<ResourceDTO>> futureFinalize = client.finalizeReduce(jId);

		await().atMost(10, TimeUnit.SECONDS).until(futureFinalize::isDone);
		assertEquals(resources, futureFinalize.get());
		verify(schedulingController).finalizeReduce(jId);
	}
}
