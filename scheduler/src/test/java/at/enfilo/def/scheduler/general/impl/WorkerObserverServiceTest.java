package at.enfilo.def.scheduler.general.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.scheduler.reducer.api.strategy.IReduceSchedulingStrategy;
import at.enfilo.def.scheduler.worker.api.strategy.ITaskSchedulingStrategy;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public abstract class WorkerObserverServiceTest {
	private INodeObserverServiceClient client;
	private IServer server;
	private Thread serverThread;

	protected ITaskSchedulingStrategy taskSchedulingStrategy;
	protected IReduceSchedulingStrategy reduceSchedulingStrategy;

	@Before
	public void setUp() throws Exception {
		TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

		// Mocking strategies
		taskSchedulingStrategy = Mockito.mock(ITaskSchedulingStrategy.class);
		reduceSchedulingStrategy = Mockito.mock(IReduceSchedulingStrategy.class);

		// Start server
		server = getServer();
		serverThread = new Thread(server);
		serverThread.start();

		// Start client
		await().atMost(10, TimeUnit.SECONDS).until(server::isRunning);
		NodeObserverServiceClientFactory factory = new NodeObserverServiceClientFactory();
		client = factory.createClient(server.getServiceEndpoint());
	}

	protected abstract IServer getServer() throws Exception;

	@After
	public void tearDown() throws Exception {
		client.close();
		server.close();
		serverThread.join();
	}


	@Test
	public void notifyTasksNewState() throws Exception {
		List<String> tIds = new LinkedList<>();
		tIds.add(UUID.randomUUID().toString());
		tIds.add(UUID.randomUUID().toString());
		String wId = UUID.randomUUID().toString();
		Random rnd = new Random();
		ExecutionState state = ExecutionState.values()[rnd.nextInt(ExecutionState.values().length)];

		Future<Void> future = client.notifyElementsNewState(wId, tIds, state);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void notifyTasksReceived() throws Exception {
		List<String> tIds = new LinkedList<>();
		tIds.add(UUID.randomUUID().toString());
		tIds.add(UUID.randomUUID().toString());
		String wId = UUID.randomUUID().toString();

		Future<Void> future = client.notifyTasksReceived(wId, tIds);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void notifyNodeInfo() throws Exception {
		String wId = UUID.randomUUID().toString();
		NodeInfoDTO infoDTO = new NodeInfoDTO();

		Future<Void> future = client.notifyNodeInfo(wId, infoDTO);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(taskSchedulingStrategy).notifyNodeInfo(wId, infoDTO);
		verify(reduceSchedulingStrategy).notifyNodeInfo(wId, infoDTO);
	}
}
