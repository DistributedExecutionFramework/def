package at.enfilo.def.node.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.node.api.INodeServiceClient;
import at.enfilo.def.node.api.NodeServiceClientFactory;
import at.enfilo.def.transfer.dto.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class NodeServiceTest {
	private IServer server;
	private Thread serverThread;
	private INodeServiceClient client;
	private NodeServiceController controller;

	@Before
	public void setUp() throws Exception {
		TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

		// Mocking internal services
		controller = Mockito.mock(NodeServiceController.class);

		// Start server
		server = getServer(controller);
		serverThread = new Thread(server);
		serverThread.start();

		await().atMost(30, TimeUnit.SECONDS).until(server::isRunning);
		NodeServiceClientFactory factory = new NodeServiceClientFactory();
		client = factory.createClient(server.getServiceEndpoint());
	}

	@After
	public void tearDown() throws Exception {
		client.close();
		server.close();
		serverThread.join();
	}

	protected abstract IServer getServer(NodeServiceController controller) throws Exception;


	@Test
	public void takeControl() throws Exception {
		String clusterId = UUID.randomUUID().toString();

		Future<Void> future = client.takeControl(clusterId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(controller).takeControl(clusterId);
	}

	@Test
	public void registerObserver() throws Exception {
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		Random rnd = new Random();
		int periodDuration = rnd.nextInt();

		Future<Void> future = client.registerObserver(endpoint, false, periodDuration, PeriodUnit.SECONDS);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(controller).registerObserver(endpoint, false, periodDuration, PeriodUnit.SECONDS);
	}

	@Test
	public void deregisterObserver() throws Exception {
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

		Future<Void> future = client.deregisterObserver(endpoint);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(controller).deregisterObserver(endpoint);
	}

	@Test
	public void getInfo() throws Exception {
		NodeInfoDTO info = new NodeInfoDTO();

		when(controller.getInfo()).thenReturn(info);

		Future<NodeInfoDTO> future = client.getInfo();
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		NodeInfoDTO requestedInfo = future.get();
		assertEquals(info, requestedInfo);
	}

	@Test
	public void getEnvironment() throws Exception {
		NodeEnvironmentDTO env = new NodeEnvironmentDTO();

		when(controller.getEnvironment()).thenReturn(env);

		Future<NodeEnvironmentDTO> future = client.getEnvironment();
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		NodeEnvironmentDTO requestedInfo = future.get();
		assertEquals(env, requestedInfo);
	}

	@Test
	public void getFeatures() throws Exception {
		List<FeatureDTO> env = new ArrayList<>();

		when(controller.getFeatures()).thenReturn(env);

		Future<List<FeatureDTO>> future = client.getFeatures();
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		List<FeatureDTO> requestedInfo = future.get();
		assertEquals(env, requestedInfo);
	}

	@Test
	public void addSharedResource() throws Exception {
		ResourceDTO sharedResource = new ResourceDTO();
		sharedResource.setId(UUID.randomUUID().toString());

		Future<Void> future = client.addSharedResource(sharedResource);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(controller).addSharedResource(sharedResource);
	}

	@Test
	public void removeSharedResources() throws Exception {
		List<String> rIds = new LinkedList<>();
		rIds.add(UUID.randomUUID().toString());
		rIds.add(UUID.randomUUID().toString());

		Future<Void> future = client.removeSharedResources(rIds);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(controller).removeSharedResources(rIds);
	}


	@Test
	public void getStoreRoutine() throws Exception {
		String rId = UUID.randomUUID().toString();

		when(controller.getStoreRoutineId()).thenReturn(rId);

		Future<String> future = client.getStoreRoutine();
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		String requestedId = future.get();
		assertEquals(rId, requestedId);
	}

	@Test
	public void setStoreRoutine() throws Exception {
		String rId = UUID.randomUUID().toString();

		Future<Void> future = client.setStoreRoutine(rId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(controller).setStoreRoutineId(rId);
	}

	@Test
	public void getQueueIds() throws Exception {
		List<String> queueIds = Arrays.asList(UUID.randomUUID().toString(), UUID.randomUUID().toString());

		when(controller.getQueueIds()).thenReturn(queueIds);

		Future<List<String>> future = client.getQueueIds();
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(queueIds, future.get());

		verify(controller).getQueueIds();
	}

	@Test
	public void getQueueInfo() throws Exception {
		String qId = UUID.randomUUID().toString();
		QueueInfoDTO queueInfo = new QueueInfoDTO();
		queueInfo.setId(qId);

		when(controller.getQueueInfo(qId)).thenReturn(queueInfo);

		Future<QueueInfoDTO> future = client.getQueueInfo(qId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(queueInfo, future.get());

		verify(controller).getQueueInfo(qId);
	}

	@Test
	public void createQueue() throws Exception {
		String qId = UUID.randomUUID().toString();

		Future<Void> future = client.createQueue(qId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(controller).createQueue(qId);
	}

	@Test
	public void deleteQueue() throws Exception {
		String qId = UUID.randomUUID().toString();

		Future<Void> future = client.deleteQueue(qId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(controller).deleteQueue(qId);
	}

	@Test
	public void pauseQueue() throws Exception {
		String qId = UUID.randomUUID().toString();

		Future<Void> future = client.pauseQueue(qId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(controller).pauseQueue(qId);
	}

	@Test
	public void releaseQueue() throws Exception {
		String qId = UUID.randomUUID().toString();

		Future<Void> future = client.releaseQueue(qId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

		verify(controller).releaseQueue(qId);
	}
}
