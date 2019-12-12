package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.api.ClusterServiceClientFactory;
import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.communication.util.ServiceRegistry;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.scheduler.api.ISchedulerServiceClient;
import at.enfilo.def.transfer.dto.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class ClusterServiceTest {

	private IClusterServiceClient clusterClient;
	private IServer server;
	private Thread serverThread;
	private Random rnd;

	protected WorkerController workerController;
	protected ReducerController reducerController;
	protected ClusterResource clusterResource;
	protected ClusterExecLogicController execLogicController;

	@Before
	public void setUp() throws Exception {
		TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());
		rnd = new Random();

		// Mocking internal services
		workerController = Mockito.mock(WorkerController.class);
		reducerController = Mockito.mock(ReducerController.class);
		clusterResource = Mockito.mock(ClusterResource.class);
		execLogicController = Mockito.mock(ClusterExecLogicController.class);

		// Start server
		server = getServer();
		serverThread = new Thread(server);
		serverThread.start();

		await().atMost(30, TimeUnit.SECONDS).until(server::isRunning);
		ClusterServiceClientFactory factory = new ClusterServiceClientFactory();
		clusterClient = factory.createClient(server.getServiceEndpoint());
	}

	@After
	public void tearDown() throws Exception {
		clusterClient.close();
		server.close();
		serverThread.join();
		ServiceRegistry.getInstance().closeAll();
	}

	protected abstract IServer getServer() throws Exception;


	@Test
	public void takeControl() throws Exception {
		String managerId = UUID.randomUUID().toString();

		Future<Void> futureTakeControl = clusterClient.takeControl(managerId);

		await().atMost(30, TimeUnit.SECONDS).until(futureTakeControl::isDone);
		assertNull(futureTakeControl.get());
		verify(clusterResource).takeControl(managerId);
	}


	@Test
	public void getClusterInfo() throws Exception {
		String clusterId = UUID.randomUUID().toString();
		Instant clusterStartTime = Instant.now();
		String managerId = UUID.randomUUID().toString();
		String name = UUID.randomUUID().toString();
		CloudType type = CloudType.COMMUNITY;

		when(clusterResource.getId()).thenReturn(clusterId);
		when(clusterResource.getName()).thenReturn(name);
		when(clusterResource.getStartTime()).thenReturn(clusterStartTime);
		when(clusterResource.getManagerId()).thenReturn(managerId);
		when(clusterResource.getCloudType()).thenReturn(type);

		Future<ClusterInfoDTO> futureClusterInfo = clusterClient.getClusterInfo();

		await().atMost(30, TimeUnit.SECONDS).until(futureClusterInfo::isDone);
		ClusterInfoDTO clusterInfo = futureClusterInfo.get();

		assertEquals(clusterId, clusterInfo.getId());
		assertEquals(clusterStartTime.toEpochMilli(), clusterInfo.getStartTime());
		assertEquals(managerId, clusterInfo.getManagerId());
		assertEquals(name, clusterInfo.getName());
		assertEquals(type, CloudType.COMMUNITY);
		verify(execLogicController).getAllPrograms();
		verify(workerController).getNodePoolSize();
	}


	@Test
	public void getAllWorkers() throws Exception {
		Future<List<String>> futureWorkers = clusterClient.getAllNodes(NodeType.WORKER);
		await().atMost(30, TimeUnit.SECONDS).until(futureWorkers::isDone);

		assertNotNull(futureWorkers.get());
		verify(workerController).getAllNodeIds();
	}


	@Test
	public void getNodeInfo() throws Exception {
		String nId = UUID.randomUUID().toString();
		NodeInfoDTO nInfo = new NodeInfoDTO();
		nInfo.setId(nId);

		when(workerController.containsNode(nId)).thenReturn(true);
		when(workerController.getNodeInfo(nId)).thenReturn(nInfo);

		Future<NodeInfoDTO> futureNode = clusterClient.getNodeInfo(nId);
		await().atMost(30, TimeUnit.SECONDS).until(futureNode::isDone);

		assertEquals(nInfo, futureNode.get());
	}

	@Test
	public void getNodeEnvironment() throws Exception {
		String nId = UUID.randomUUID().toString();
		String nId2 = UUID.randomUUID().toString();
		String nId3 = UUID.randomUUID().toString();

		when(workerController.getAllNodeIds()).thenReturn(Arrays.asList(nId, nId2));
		when(reducerController.getAllNodeIds()).thenReturn(Collections.singletonList(nId3));
		when(workerController.containsNode(nId)).thenReturn(true);
		when(workerController.containsNode(nId2)).thenReturn(true);
		when(reducerController.containsNode(nId3)).thenReturn(true);
		when(workerController.getNodeEnvironment(nId)).thenReturn(getTestFeature());
		when(workerController.getNodeEnvironment(nId2)).thenReturn(getTestFeature());
		when(reducerController.getNodeEnvironment(nId3)).thenReturn(getTestFeature());

		Future<List<FeatureDTO>> futureNode = clusterClient.getEnvironment();
		await().atMost(30, TimeUnit.SECONDS).until(futureNode::isDone);

		List<FeatureDTO> result = futureNode.get();
		FeatureDTO featureDTO = getTestFeature().get(0);
		assertEquals(3, result.size());
		assertEquals(featureDTO.getName(), result.get(0).getName());
		assertEquals(featureDTO.getName(), result.get(1).getName());
		assertEquals(featureDTO.getName(), result.get(2).getName());
	}

	@Test
	public void getEnvironment() throws Exception {
		String nId = UUID.randomUUID().toString();

		when(workerController.containsNode(nId)).thenReturn(true);
		when(workerController.getNodeEnvironment(nId)).thenReturn(getTestFeature());

		Future<List<FeatureDTO>> futureNode = clusterClient.getNodeEnvironment(nId);
		await().atMost(30, TimeUnit.SECONDS).until(futureNode::isDone);

		assertEquals(getTestFeature().get(0).getName(), futureNode.get().get(0).getName());
	}

	private List<FeatureDTO> getTestFeature() {
		FeatureDTO featureDTO = new FeatureDTO();
		featureDTO.setGroup("language");
		featureDTO.setName("java");
		featureDTO.setVersion("1.8");
		return Collections.singletonList(featureDTO);
	}

	@Test
	public void addWorker() throws Exception {
		ServiceEndpointDTO se = new ServiceEndpointDTO();

		Future<Void> futureAddWorker = clusterClient.addNode(se, NodeType.WORKER);
		await().atMost(30, TimeUnit.SECONDS).until(futureAddWorker::isDone);

		assertNull(futureAddWorker.get());
		verify(workerController).addNode(se);
	}

	@Test
	public void removeNode() throws Exception {
		String wId = UUID.randomUUID().toString();

		when(workerController.containsNode(wId)).thenReturn(true);
		Future<Void> futureRemoveWorker = clusterClient.removeNode(wId);
		await().atMost(30, TimeUnit.SECONDS).until(futureRemoveWorker::isDone);

		assertNull(futureRemoveWorker.get());
		verify(workerController).removeNode(wId, true);
	}

	@Test
	public void getNodeServiceEndpoint() throws Exception {
		String wId = UUID.randomUUID().toString();

		when(workerController.containsNode(wId)).thenReturn(true);
		Future<ServiceEndpointDTO> futureServiceEndpoint = clusterClient.getNodeServiceEndpoint(wId);
		await().atMost(30, TimeUnit.SECONDS).until(futureServiceEndpoint::isDone);

		verify(workerController).getNodeServiceEndpoint(wId);
	}

	@Test
	public void getSchedulerServiceEndpoint() throws Exception {
		ServiceEndpointDTO se = new ServiceEndpointDTO();

		ISchedulerServiceClient schedulerClientMock = Mockito.mock(ISchedulerServiceClient.class);
		when(schedulerClientMock.getServiceEndpoint()).thenReturn(se);
		when(clusterResource.getSchedulerServiceClient(NodeType.WORKER)).thenReturn(schedulerClientMock);

		Future<ServiceEndpointDTO> futureServiceEndpoint = clusterClient.getSchedulerServiceEndpoint(
			NodeType.WORKER
		);
		await().atMost(30, TimeUnit.SECONDS).until(futureServiceEndpoint::isDone);

		assertEquals(se, futureServiceEndpoint.get());
	}

	@Test
	public void setSchedulerServiceEndpoint() throws Exception {
		ServiceEndpointDTO se = new ServiceEndpointDTO();

		Future<Void> future = clusterClient.setSchedulerServiceEndpoint(
			NodeType.WORKER,
			se
		);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		verify(clusterResource).setSchedulerService(
			NodeType.WORKER,
			se
		);
	}

	@Test
	public void getDefaultMapRoutine() throws Exception {
		String routineId = UUID.randomUUID().toString();

		when(clusterResource.getDefaultMapRoutineId()).thenReturn(routineId);
		Future<String> future = clusterClient.getDefaultMapRoutine();
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(routineId, future.get());
	}

	@Test
	public void setDefaultMapRoutine() throws Exception {
		String routineId = UUID.randomUUID().toString();

		Future<Void> future = clusterClient.setDefaultMapRoutine(routineId);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertNull(future.get());
		verify(clusterResource).setDefaultMapRoutineId(routineId);
	}

	@Test
	public void getStoreRoutine() throws Exception {
		String routineId = UUID.randomUUID().toString();

		when(workerController.getStoreRoutineId()).thenReturn(routineId);
		Future<String> future = clusterClient.getStoreRoutine();
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(routineId, future.get());
	}

	@Test
	public void setStoreRoutine() throws Exception {
		String routineId = UUID.randomUUID().toString();

		Future<Void> future = clusterClient.setStoreRoutine(routineId);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertNull(future.get());
		verify(workerController).setStoreRoutineId(routineId);
	}

	@Test
	public void getLibraryEndpointConfiguration() throws Exception {
		Future<ServiceEndpointDTO> future = clusterClient.getLibraryEndpointConfiguration();
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		ServiceEndpointDTO libraryEndpoint = future.get();
		assertNotNull(libraryEndpoint);
		assertNotNull(libraryEndpoint.getProtocol());
		assertNotEquals(0, libraryEndpoint.getPort());
		assertNotNull(libraryEndpoint.getPathPrefix());
	}

	@Test
	public void getNodeEndpointConfiguration() throws Exception {
		Future<ServiceEndpointDTO> future = clusterClient.getNodeServiceEndpointConfiguration(NodeType.WORKER);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		ServiceEndpointDTO nodeEndpoint = future.get();
		assertNotNull(nodeEndpoint);
		assertNotNull(nodeEndpoint.getProtocol());
		assertNotEquals(0, nodeEndpoint.getPort());
		assertNotNull(nodeEndpoint.getPathPrefix());
	}

}
