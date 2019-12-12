package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.api.ClusterServiceClientFactory;
import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.TicketFailedException;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.communication.util.ServiceRegistry;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.scheduler.clientroutineworker.api.IClientRoutineWorkerSchedulerServiceClient;
import at.enfilo.def.scheduler.reducer.api.IReducerSchedulerServiceClient;
import at.enfilo.def.scheduler.worker.api.IWorkerSchedulerServiceClient;
import at.enfilo.def.transfer.dto.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
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

	protected WorkerController workerController;
	protected ReducerController reducerController;
	protected ClientRoutineWorkerController clientRoutineWorkerController;
	protected ClusterResource clusterResource;
	protected ClusterExecLogicController execLogicController;

	@Before
	public void setUp() throws Exception {
		TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

		// Mocking internal services
		workerController = Mockito.mock(WorkerController.class);
		reducerController = Mockito.mock(ReducerController.class);
		clientRoutineWorkerController = Mockito.mock(ClientRoutineWorkerController.class);
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
	public void getAllReducers() throws Exception {
		Future<List<String>> futureReducers = clusterClient.getAllNodes(NodeType.REDUCER);
		await().atMost(30, TimeUnit.SECONDS).until(futureReducers::isDone);

		assertNotNull(futureReducers.get());
		verify(reducerController).getAllNodeIds();
	}

	@Test
	public void getAllClientRoutineWorkers() throws Exception {
		Future<List<String>> future = clusterClient.getAllNodes(NodeType.CLIENT);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertNotNull(future.get());
		verify(clientRoutineWorkerController).getAllNodeIds();
	}

	@Test
	public void getWorkerInfo() throws Exception {
		String wId = UUID.randomUUID().toString();
		NodeInfoDTO wInfo = new NodeInfoDTO();
		wInfo.setId(wId);

		when(workerController.containsNode(wId)).thenReturn(true);
		when(workerController.getNodeInfo(wId)).thenReturn(wInfo);

		Future<NodeInfoDTO> futureNode = clusterClient.getNodeInfo(wId);
		await().atMost(30, TimeUnit.SECONDS).until(futureNode::isDone);

		assertEquals(wInfo, futureNode.get());
		verify(workerController).containsNode(wId);
		verify(workerController).getNodeInfo(wId);
	}

	@Test
	public void getReducerInfo() throws Exception {
		String rId = UUID.randomUUID().toString();
		NodeInfoDTO rInfo = new NodeInfoDTO();
		rInfo.setId(rId);

		when(workerController.containsNode(rId)).thenReturn(false);
		when(reducerController.containsNode(rId)).thenReturn(true);
		when(reducerController.getNodeInfo(rId)).thenReturn(rInfo);

		Future<NodeInfoDTO> future = clusterClient.getNodeInfo(rId);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(rInfo, future.get());
		verify(reducerController).containsNode(rId);
		verify(reducerController).getNodeInfo(rId);
	}

	@Test
	public void getClientRoutineWorkerInfo() throws Exception {
		String wId = UUID.randomUUID().toString();
		NodeInfoDTO wInfo = new NodeInfoDTO();
		wInfo.setId(wId);

		when(workerController.containsNode(wId)).thenReturn(false);
		when(reducerController.containsNode(wId)).thenReturn(false);
		when(clientRoutineWorkerController.containsNode(wId)).thenReturn(true);
		when(clientRoutineWorkerController.getNodeInfo(wId)).thenReturn(wInfo);

		Future<NodeInfoDTO> future = clusterClient.getNodeInfo(wId);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(wInfo, future.get());
		verify(clientRoutineWorkerController).containsNode(wId);
		verify(clientRoutineWorkerController).getNodeInfo(wId);
	}

	@Test (expected = ExecutionException.class)
	public void getNodeInfo_failed() throws Exception {
		String nId = UUID.randomUUID().toString();

		when(workerController.containsNode(nId)).thenReturn(false);
		when(reducerController.containsNode(nId)).thenReturn(false);
		Future<NodeInfoDTO> future = clusterClient.getNodeInfo(UUID.randomUUID().toString());
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(TicketStatusDTO.FAILED, future.get());
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

		when(workerController.addWorker(se)).thenReturn(UUID.randomUUID().toString());
		Future<Void> futureAddWorker = clusterClient.addNode(se, NodeType.WORKER);
		await().atMost(30, TimeUnit.SECONDS).until(futureAddWorker::isDone);

		verify(workerController).addWorker(se);
	}

	@Test
	public void addReducer() throws Exception {
		ServiceEndpointDTO se = new ServiceEndpointDTO();

		when(reducerController.addReducer(se)).thenReturn(UUID.randomUUID().toString());
		Future<Void> futureAddReducer = clusterClient.addNode(se, NodeType.REDUCER);
		await().atMost(30, TimeUnit.SECONDS).until(futureAddReducer::isDone);

		verify(reducerController).addReducer(se);
	}

	@Test
	public void addClientRoutineWorker() throws Exception {
		ServiceEndpointDTO se = new ServiceEndpointDTO();

		when(clientRoutineWorkerController.addClientRoutineWorker(se)).thenReturn(UUID.randomUUID().toString());
		Future<Void> future = clusterClient.addNode(se, NodeType.CLIENT);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		verify(clientRoutineWorkerController).addClientRoutineWorker(se);
	}

	@Test
	public void removeWorker() throws Exception {
		String wId = UUID.randomUUID().toString();

		when(workerController.containsNode(wId)).thenReturn(true);
		Future<Void> futureRemoveWorker = clusterClient.removeNode(wId);
		await().atMost(30, TimeUnit.SECONDS).until(futureRemoveWorker::isDone);

		verify(workerController).removeNode(wId);
	}

	@Test
	public void removeReducer() throws Exception {
		String rId = UUID.randomUUID().toString();

		when(workerController.containsNode(rId)).thenReturn(false);
		when(reducerController.containsNode(rId)).thenReturn(true);
		Future<Void> futureRemoveReducer = clusterClient.removeNode(rId);
		await().atMost(30, TimeUnit.SECONDS).until(futureRemoveReducer::isDone);

		verify(reducerController).removeNode(rId);
	}

	@Test
	public void removeClientRoutineWorker() throws Exception {
		String wId = UUID.randomUUID().toString();

		when(workerController.containsNode(wId)).thenReturn(false);
		when(reducerController.containsNode(wId)).thenReturn(false);
		when(clientRoutineWorkerController.containsNode(wId)).thenReturn(true);
		Future<Void> future = clusterClient.removeNode(wId);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		verify(clientRoutineWorkerController).removeNode(wId);
	}

	@Test(expected = TicketFailedException.class)
	public void removeNode_failed() throws Exception {
		String nId = UUID.randomUUID().toString();

		when(workerController.containsNode(nId)).thenReturn(false);
		when(reducerController.containsNode(nId)).thenReturn(false);
		Future<Void> future = clusterClient.removeNode(nId);
		future.get();
	}

	@Test
	public void getWorkerServiceEndpoint() throws Exception {
		String wId = UUID.randomUUID().toString();

		when(workerController.containsNode(wId)).thenReturn(true);
		Future<ServiceEndpointDTO> futureServiceEndpoint = clusterClient.getNodeServiceEndpoint(wId);
		await().atMost(30, TimeUnit.SECONDS).until(futureServiceEndpoint::isDone);

		verify(workerController).getNodeServiceEndpoint(wId);
	}

	@Test
	public void getReducerServiceEndpoint() throws Exception {
		String rId = UUID.randomUUID().toString();

		when(workerController.containsNode(rId)).thenReturn(false);
		when(reducerController.containsNode(rId)).thenReturn(true);
		Future<ServiceEndpointDTO> futureServiceEndpoint = clusterClient.getNodeServiceEndpoint(rId);
		await().atMost(30, TimeUnit.SECONDS).until(futureServiceEndpoint::isDone);

		verify(reducerController).getNodeServiceEndpoint(rId);
	}

	@Test
	public void getClientRoutineWorkerServiceEndpoint() throws Exception {
		String wId = UUID.randomUUID().toString();

		when(workerController.containsNode(wId)).thenReturn(false);
		when(reducerController.containsNode(wId)).thenReturn(false);
		when(clientRoutineWorkerController.containsNode(wId)).thenReturn(true);
		Future<ServiceEndpointDTO> future = clusterClient.getNodeServiceEndpoint(wId);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		verify(clientRoutineWorkerController).getNodeServiceEndpoint(wId);
	}

	@Test (expected = ExecutionException.class)
	public void getNodeServiceEndpoint_failed() throws Exception {
		String nId = UUID.randomUUID().toString();

		when(workerController.containsNode(nId)).thenReturn(false);
		when(reducerController.containsNode(nId)).thenReturn(false);
		Future<ServiceEndpointDTO> future = clusterClient.getNodeServiceEndpoint(nId);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(TicketStatusDTO.FAILED, future.get());
	}

	@Test
	public void getWorkerSchedulerServiceEndpoint() throws Exception {
		ServiceEndpointDTO se = new ServiceEndpointDTO();

		IWorkerSchedulerServiceClient schedulerClientMock = Mockito.mock(IWorkerSchedulerServiceClient.class);
		when(schedulerClientMock.getServiceEndpoint()).thenReturn(se);
		when(clusterResource.getWorkerSchedulerServiceClient()).thenReturn(schedulerClientMock);

		Future<ServiceEndpointDTO> futureServiceEndpoint = clusterClient.getSchedulerServiceEndpoint(
			NodeType.WORKER
		);
		await().atMost(30, TimeUnit.SECONDS).until(futureServiceEndpoint::isDone);

		assertEquals(se, futureServiceEndpoint.get());
		verify(clusterResource).getWorkerSchedulerServiceClient();
	}

	@Test
	public void getReducerSchedulerServiceEndpoint() throws Exception {
		ServiceEndpointDTO se = new ServiceEndpointDTO();

		IReducerSchedulerServiceClient schedulerClientMock = Mockito.mock(IReducerSchedulerServiceClient.class);
		when(schedulerClientMock.getServiceEndpoint()).thenReturn(se);
		when(clusterResource.getReducerSchedulerServiceClient()).thenReturn(schedulerClientMock);

		Future<ServiceEndpointDTO> future = clusterClient.getSchedulerServiceEndpoint(
				NodeType.REDUCER
		);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(se, future.get());
		verify(clusterResource).getReducerSchedulerServiceClient();
	}

	@Test
	public void getClientRoutineWorkerSchedulerServiceEndpoint() throws Exception {
		ServiceEndpointDTO se = new ServiceEndpointDTO();

		IClientRoutineWorkerSchedulerServiceClient schedulerClientMock = Mockito.mock(IClientRoutineWorkerSchedulerServiceClient.class);
		when(schedulerClientMock.getServiceEndpoint()).thenReturn(se);
		when(clusterResource.getClientRoutineWorkerSchedulerSerivceClient()).thenReturn(schedulerClientMock);

		Future<ServiceEndpointDTO> future = clusterClient.getSchedulerServiceEndpoint(
				NodeType.CLIENT
		);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(se, future.get());
		verify(clusterResource).getClientRoutineWorkerSchedulerSerivceClient();
	}

	@Test
	public void setWorkerSchedulerServiceEndpoint() throws Exception {
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
		verify(workerController).addAllNodesToScheduler();
	}

	@Test
	public void setReducerSchedulerServiceEndpoint() throws Exception {
		ServiceEndpointDTO se = new ServiceEndpointDTO();

		Future<Void> future = clusterClient.setSchedulerServiceEndpoint(
				NodeType.REDUCER,
				se
		);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		verify(clusterResource).setSchedulerService(
				NodeType.REDUCER,
				se
		);
		verify(reducerController).addAllNodesToScheduler();
	}

	@Test
	public void setClientRoutineWorkerSchedulerServiceEndpoint() throws Exception {
		ServiceEndpointDTO se = new ServiceEndpointDTO();

		Future<Void> future = clusterClient.setSchedulerServiceEndpoint(
				NodeType.CLIENT,
				se
		);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		verify(clusterResource).setSchedulerService(
				NodeType.CLIENT,
				se
		);
		verify(clientRoutineWorkerController).addAllNodesToScheduler();
	}

	@Test
	public void getDefaultMapRoutine() throws Exception {
		String routineId = UUID.randomUUID().toString();

		when(clusterResource.getDefaultMapRoutineId()).thenReturn(routineId);
		Future<String> future = clusterClient.getDefaultMapRoutine();
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(routineId, future.get());
		verify(clusterResource).getDefaultMapRoutineId();
	}

	@Test
	public void setDefaultMapRoutine() throws Exception {
		String routineId = UUID.randomUUID().toString();

		Future<Void> future = clusterClient.setDefaultMapRoutine(routineId);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		verify(clusterResource).setDefaultMapRoutineId(routineId);
	}

	@Test
	public void getStoreRoutineWorker() throws Exception {
		String routineId = UUID.randomUUID().toString();

		when(workerController.getStoreRoutineId()).thenReturn(routineId);
		Future<String> future = clusterClient.getStoreRoutine(NodeType.WORKER);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(routineId, future.get());
		verify(workerController).getStoreRoutineId();
	}

	@Test
	public void getStoreRoutineReducer() throws Exception {
		String routineId = UUID.randomUUID().toString();

		when(reducerController.getStoreRoutineId()).thenReturn(routineId);
		Future<String> future = clusterClient.getStoreRoutine(NodeType.REDUCER);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(routineId, future.get());
		verify(reducerController).getStoreRoutineId();
	}

	@Test
	public void getStoreRoutineClientRoutineWorker() throws Exception {
		String routineId = UUID.randomUUID().toString();

		when(clientRoutineWorkerController.getStoreRoutineId()).thenReturn(routineId);
		Future<String> future = clusterClient.getStoreRoutine(NodeType.CLIENT);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(routineId, future.get());
		verify(clientRoutineWorkerController).getStoreRoutineId();
	}

	@Test
	public void setStoreRoutineWorker() throws Exception {
		String routineId = UUID.randomUUID().toString();

		Future<Void> future = clusterClient.setStoreRoutine(routineId, NodeType.WORKER);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		verify(workerController).setStoreRoutineId(routineId);
	}

	@Test
	public void setStoreRoutineReducer() throws Exception {
		String routineId = UUID.randomUUID().toString();

		Future<Void> future = clusterClient.setStoreRoutine(routineId, NodeType.REDUCER);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		verify(reducerController).setStoreRoutineId(routineId);
	}

	@Test
	public void setStoreRoutineClientRoutineWorker() throws Exception {
		String routineId = UUID.randomUUID().toString();

		Future<Void> future = clusterClient.setStoreRoutine(routineId, NodeType.CLIENT);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		verify(clientRoutineWorkerController).setStoreRoutineId(routineId);
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
	public void getWorkerEndpointConfiguration() throws Exception {
		Future<ServiceEndpointDTO> future = clusterClient.getNodeServiceEndpointConfiguration(NodeType.WORKER);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		ServiceEndpointDTO nodeEndpoint = future.get();
		assertNotNull(nodeEndpoint);
		assertNotNull(nodeEndpoint.getProtocol());
		assertNotEquals(0, nodeEndpoint.getPort());
		assertNotNull(nodeEndpoint.getPathPrefix());
	}

	@Test
	public void getReducerEndpointConfiguration() throws Exception {
		Future<ServiceEndpointDTO> future = clusterClient.getNodeServiceEndpointConfiguration(NodeType.REDUCER);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		ServiceEndpointDTO nodeEndpoint = future.get();
		assertNotNull(nodeEndpoint);
		assertNotNull(nodeEndpoint.getProtocol());
		assertNotEquals(0, nodeEndpoint.getPort());
		assertNotNull(nodeEndpoint.getPathPrefix());
	}

	@Test
	public void getClientRoutineWorkerEndpointConfiguration() throws Exception {
		Future<ServiceEndpointDTO> future = clusterClient.getNodeServiceEndpointConfiguration(NodeType.CLIENT);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		ServiceEndpointDTO nodeEndpoint = future.get();
		assertNotNull(nodeEndpoint);
		assertNotNull(nodeEndpoint.getProtocol());
		assertNotEquals(0, nodeEndpoint.getPort());
		assertNotNull(nodeEndpoint.getPathPrefix());
	}

	@Test
	public void findWorkersForShutdown() throws Exception {
		int nrOfNodes = 2;
		List<String> ids = new LinkedList<>();
		ids.add(UUID.randomUUID().toString());
		ids.add(UUID.randomUUID().toString());

		when(workerController.findNodesForShutdown(nrOfNodes)).thenReturn(ids);
		Future<List<String>> future = clusterClient.findNodesForShutdown(NodeType.WORKER, nrOfNodes);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(ids, future.get());
		verify(workerController).findNodesForShutdown(nrOfNodes);
	}

	@Test
	public void findReducersForShutdown() throws Exception {
		int nrOfNodes = 2;
		List<String> ids = new LinkedList<>();
		ids.add(UUID.randomUUID().toString());
		ids.add(UUID.randomUUID().toString());

		when(reducerController.findNodesForShutdown(nrOfNodes)).thenReturn(ids);
		Future<List<String>> future = clusterClient.findNodesForShutdown(NodeType.REDUCER, nrOfNodes);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(ids, future.get());
		verify(reducerController).findNodesForShutdown(nrOfNodes);
	}

	@Test
	public void findClientRoutineWorkersForShutdown() throws Exception {
		int nrOfNodes = 2;
		List<String> ids = new LinkedList<>();
		ids.add(UUID.randomUUID().toString());
		ids.add(UUID.randomUUID().toString());

		when(clientRoutineWorkerController.findNodesForShutdown(nrOfNodes)).thenReturn(ids);
		Future<List<String>> future = clusterClient.findNodesForShutdown(NodeType.CLIENT, nrOfNodes);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);

		assertEquals(ids, future.get());
		verify(clientRoutineWorkerController).findNodesForShutdown(nrOfNodes);
	}
}
