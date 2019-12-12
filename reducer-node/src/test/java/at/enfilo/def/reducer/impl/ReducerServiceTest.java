package at.enfilo.def.reducer.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.reducer.api.IReducerServiceClient;
import at.enfilo.def.reducer.api.ReducerServiceClientFactory;
import at.enfilo.def.transfer.dto.ResourceDTO;
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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class ReducerServiceTest {
	private IServer server;
	private Thread serverThread;
	private IReducerServiceClient client;
	private ReducerServiceController controller;

	@Before
	public void setUp() throws Exception {
		TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

		// Mocking internal services
		controller = Mockito.mock(ReducerServiceController.class);

		// Start server
		server = getServer(controller);
		serverThread = new Thread(server);
		serverThread.start();

		await().atMost(30, TimeUnit.SECONDS).until(server::isRunning);
		ReducerServiceClientFactory factory = new ReducerServiceClientFactory();
		client = factory.createClient(server.getServiceEndpoint());
	}

	@After
	public void tearDown() throws Exception {
		client.close();
		server.close();
		serverThread.join();
	}

	protected abstract IServer getServer(ReducerServiceController controller) throws Exception;


	@Test
	public void createReduceJob() throws Exception {
		String jId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		Future<Void> future = client.createReduceJob(jId, routineId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());

		verify(controller).createReduceJob(jId, routineId);
	}

	@Test
	public void add() throws Exception {
		String jId = UUID.randomUUID().toString();
		List<ResourceDTO> resources = new LinkedList<>();
		resources.add(new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));
		resources.add(new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));

		Future<Void> future = client.add(jId, resources);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());

		verify(controller).addResources(jId, resources);
	}

	@Test
	public void reduce() throws Exception {
		String jId = UUID.randomUUID().toString();

		Future<Void> future = client.reduce(jId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());

		verify(controller).reduce(jId);
	}

	@Test
	public void fetchResult() throws Exception {
		String jId = UUID.randomUUID().toString();
		List<ResourceDTO> results = new LinkedList<>();
		results.add(new ResourceDTO(UUID.randomUUID().toString(), UUID.randomUUID().toString()));

		when(controller.fetchResult(jId)).thenReturn(results);

		Future<List<ResourceDTO>> future = client.fetchResult(jId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		List<ResourceDTO> requestedResults = future.get();
		assertEquals(results, requestedResults);
	}
}
