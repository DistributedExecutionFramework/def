package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.api.NodeCreationException;
import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.util.ClusterConfiguration;
import at.enfilo.def.cluster.util.WorkersConfiguration;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.node.observer.api.util.NodeNotificationConfiguration;
import at.enfilo.def.scheduler.api.ISchedulerServiceClient;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class WorkerControllerTest {

	private WorkerController nodeController;
	private ClusterConfiguration configuration = Cluster.getInstance().getConfiguration();
	private WorkerServiceClientFactory workerServiceClientFactoryMock;
	private ISchedulerServiceClient schedulerServiceClientMock;
	private IWorkerServiceClient workerServiceClientMock;
	private List<String> workers;
	private Map<String, String> workerInstanceMap;
	private Map<String, IWorkerServiceClient> workerConnectionMap;
	private Map<String, NodeInfoDTO> workerInfoMap;
	private Map<String, List<FeatureDTO>> workerFeatureMap;
	private Map<String, Set<String>> workerTaskAssignment;

	@Before
	public void setUp() throws Exception {
		// Creating general mocks
		workerServiceClientFactoryMock = Mockito.mock(WorkerServiceClientFactory.class);
		workerServiceClientMock = Mockito.mock(IWorkerServiceClient.class);
		schedulerServiceClientMock = Mockito.mock(ISchedulerServiceClient.class);

		ClusterResource.getInstance().setWorkerSchedulerServiceClient(schedulerServiceClientMock);

		// Create lists and maps for NodeController
		workers = new LinkedList<>();
		workerInstanceMap = new HashMap<>();
		workerConnectionMap = new HashMap<>();
		workerInfoMap = new HashMap<>();
		workerFeatureMap = new HashMap<>();
		workerTaskAssignment = new HashMap<>();

		// Create controller with special constructor
		Constructor<WorkerController> constructor = WorkerController.class.getDeclaredConstructor(
				WorkerServiceClientFactory.class,
				List.class,
				Map.class,
				Map.class,
				Map.class,
				Map.class,
				WorkersConfiguration.class,
				Map.class
				);
		constructor.setAccessible(true);
		nodeController = constructor.newInstance(
				workerServiceClientFactoryMock,
				workers,
				workerInstanceMap,
				workerConnectionMap,
				workerInfoMap,
				workerFeatureMap,
				Cluster.getInstance().getConfiguration().getWorkersConfiguration(),
				workerTaskAssignment
		);
	}


	@Test
	public void getWorkerInfo() throws Exception {
		String workerId = UUID.randomUUID().toString();
		NodeInfoDTO info = new NodeInfoDTO(
			workerId,
			ClusterResource.getInstance().getId(),
			NodeType.WORKER,
			-1,
			-1,
			-1,
			new HashMap<>(),
			"localhost"
		);
		workers.add(workerId);
		workerInfoMap.put(workerId, info);

		NodeInfoDTO receivedInfo = nodeController.getNodeInfo(workerId);

		assertEquals(info, receivedInfo);
	}


	@Test(expected = UnknownNodeException.class)
	public void getUnknownWorkerInfo() throws Exception {
		nodeController.getNodeInfo(UUID.randomUUID().toString());
	}

	@Test
	public void getServiceClient() throws Exception {
		String workerId = UUID.randomUUID().toString();

		workers.add(workerId);
		workerConnectionMap.put(workerId, workerServiceClientMock);

		IWorkerServiceClient client = nodeController.getServiceClient(workerId);
		assertEquals(workerServiceClientMock, client);
	}

	@Test(expected = UnknownNodeException.class)
	public void getUnknownWorkerClient() throws Exception {
		String workerId = UUID.randomUUID().toString();
		String unknownWorkerId = UUID.randomUUID().toString();

		workers.add(workerId);
		workerConnectionMap.put(workerId, workerServiceClientMock);

		IWorkerServiceClient client = nodeController.getServiceClient(unknownWorkerId);
	}

	@Test
	public void addWorker() throws Exception {
		NodeInfoDTO workerInfo = new NodeInfoDTO();
		workerInfo.setId(UUID.randomUUID().toString());
		workerInfo.setClusterId(ClusterResource.getInstance().getId());

		FeatureDTO featureDTO = new FeatureDTO();
		featureDTO.setName("java");
		featureDTO.setVersion("1.8");
		featureDTO.setGroup("language");
		List<FeatureDTO> featureDTOList = Collections.singletonList(featureDTO);

		ServiceEndpointDTO serviceEndpoint = new ServiceEndpointDTO();
		Future<Void> futureStatus = Mockito.mock(Future.class);
		Future<NodeInfoDTO> futureInfo = Mockito.mock(Future.class);
		Future<List<FeatureDTO>> futureEnvironment = Mockito.mock(Future.class);


		// Setup observer config / service endpoint
		NodeNotificationConfiguration notificationConfig = configuration.getWorkersConfiguration().getNotificationFromNode();
		ServiceEndpointDTO observerEndpoint = notificationConfig.getEndpoint();
		observerEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());

		// Mock actions
		when(workerServiceClientFactoryMock.createClient(anyObject())).thenReturn(workerServiceClientMock);
		when(workerServiceClientMock.takeControl(anyString())).thenReturn(futureStatus);
		when(futureStatus.get()).thenReturn(null);
		when(workerServiceClientMock.getInfo()).thenReturn(futureInfo);
		when(futureInfo.get()).thenReturn(workerInfo);
		when(workerServiceClientMock.getFeatures()).thenReturn(futureEnvironment);
		when(futureEnvironment.get()).thenReturn(featureDTOList);
		Future<Void> futureSetWorkers = Mockito.mock(Future.class);
		when(schedulerServiceClientMock.addWorker(workerInfo.getId(), serviceEndpoint)).thenReturn(futureSetWorkers);
		when(futureSetWorkers.get()).thenReturn(null);
		Future<Void> futureRegisterObserver = Mockito.mock(Future.class);
		when(workerServiceClientMock.registerObserver(
				observerEndpoint,
				notificationConfig.isPeriodically(),
				notificationConfig.getPeriodDuration(),
				notificationConfig.getPeriodUnit()
		)).thenReturn(futureRegisterObserver);
		when(futureRegisterObserver.get()).thenReturn(null);
		when(workerServiceClientMock.setStoreRoutine(anyString())).thenReturn(futureStatus);

		nodeController.addNode(serviceEndpoint);

		assertEquals(1, workers.size());
		assertTrue(workerInfoMap.containsKey(workerInfo.getId()));
		assertTrue(workerFeatureMap.containsKey(workerInfo.getId()));
		verify(schedulerServiceClientMock).addWorker(workerInfo.getId(), serviceEndpoint);
	}

	@Test(expected = NodeCreationException.class)
	public void addWorkerFailed() throws Exception {
		NodeInfoDTO workerInfo = new NodeInfoDTO();
		workerInfo.setId(UUID.randomUUID().toString());

		ServiceEndpointDTO serviceEndpoint = new ServiceEndpointDTO();
		Future<Void> futureStatus = Mockito.mock(Future.class);

		when(workerServiceClientFactoryMock.createClient(anyObject())).thenReturn(workerServiceClientMock);
		when(workerServiceClientMock.takeControl(anyString())).thenReturn(futureStatus);
		when(futureStatus.get()).thenThrow(new ExecutionException("failed", null));

		nodeController.addNode(serviceEndpoint);
	}


	@Test
	public void notifyNodeInfo() throws Exception {
		// Prepare a worker
		String nId = UUID.randomUUID().toString();
		NodeInfoDTO nodeInfo = new NodeInfoDTO();
		nodeInfo.setParameters(new HashMap<>());
		nodeInfo.setId(nId);
		nodeInfo.getParameters().put("numberOfQueuedTasks", "1");
		nodeInfo.setLoad(1.1);

		workers.add(nId);
		workerInfoMap.put(nId, nodeInfo);

		assertEquals(nodeInfo, nodeController.getNodeInfo(nId));

		// New nodeInfo + notification
		NodeInfoDTO newNodeInfo = new NodeInfoDTO();
		newNodeInfo.setId(nId);
		nodeInfo.getParameters().put("numberOfQueuedTasks", "4");
		newNodeInfo.setLoad(2.1);
		nodeController.notifyNodeInfo(nId, newNodeInfo);
		assertEquals(newNodeInfo, nodeController.getNodeInfo(nId));
	}


	@Test(expected = UnknownNodeException.class)
	public void notifyWorkerInfoFailed() throws Exception {
		NodeInfoDTO nodeInfo = new NodeInfoDTO();
		nodeController.notifyNodeInfo(UUID.randomUUID().toString(), nodeInfo);
	}

	@Test
	public void notifyTasksReceived() throws Exception {
		// Prepare Worker
		String nId = UUID.randomUUID().toString();
		NodeInfoDTO nInfo = new NodeInfoDTO();
		nInfo.setId(nId);

		workerInfoMap.put(nId, nInfo);
		workers.add(nId);

		assertTrue(workerTaskAssignment.isEmpty());

		List<String> tIds = new LinkedList<>();
		String t1Id = UUID.randomUUID().toString();
		tIds.add(t1Id);
		String t2Id = UUID.randomUUID().toString();
		tIds.add(t2Id);

		// Notify and proof
		nodeController.notifyTasksReceived(nId, tIds);
		assertFalse(workerTaskAssignment.isEmpty());
		assertNotNull(workerTaskAssignment.get(nId));
		assertTrue(workerTaskAssignment.get(nId).contains(t1Id));
		assertTrue(workerTaskAssignment.get(nId).contains(t2Id));
	}

	@Test
	public void notifyTasksSuccess() throws Exception {
		// Prepare Worker and assignments
		String nId = UUID.randomUUID().toString();
		NodeInfoDTO nInfo = new NodeInfoDTO();
		nInfo.setId(nId);

		workerInfoMap.put(nId, nInfo);
		workers.add(nId);

		workerTaskAssignment.put(nId, new HashSet<>());
		String t1Id = UUID.randomUUID().toString();
		String t2Id = UUID.randomUUID().toString();
		String t3Id = UUID.randomUUID().toString();
		workerTaskAssignment.get(nId).add(t1Id);
		workerTaskAssignment.get(nId).add(t2Id);
		workerTaskAssignment.get(nId).add(t3Id);

		List<String> tIds = new LinkedList<>();
		tIds.add(t1Id);
		tIds.add(t3Id);

		// Notify and proof
		nodeController.notifyTasksNewState(nId, tIds, ExecutionState.SUCCESS);
		assertFalse(workerTaskAssignment.isEmpty());
		assertFalse(workerTaskAssignment.get(nId).contains(t1Id));
		assertTrue(workerTaskAssignment.get(nId).contains(t2Id));
		assertFalse(workerTaskAssignment.get(nId).contains(t3Id));
	}

	@Test
	public void removeWorker() throws Exception {
		// Prepare Workers
		String nId1 = UUID.randomUUID().toString();
		NodeInfoDTO nInfo1 = new NodeInfoDTO();
		nInfo1.setId(nId1);
		String nId2 = UUID.randomUUID().toString();
		NodeInfoDTO wInfo2 = new NodeInfoDTO();
		wInfo2.setId(nId2);
		String nId3 = UUID.randomUUID().toString();
		NodeInfoDTO nInfo3 = new NodeInfoDTO();
		nInfo3.setId(nId3);

		workers.add(nId1);
		workers.add(nId2);
		workers.add(nId3);
		workerInfoMap.put(nId1, nInfo1);
		workerInfoMap.put(nId2, wInfo2);
		workerInfoMap.put(nId3, nInfo3);
		workerInstanceMap.put(nId1, null);
		workerInstanceMap.put(nId2, null);
		workerInstanceMap.put(nId3, null);
		workerConnectionMap.put(nId1, workerServiceClientMock);
		workerConnectionMap.put(nId2, workerServiceClientMock);
		workerConnectionMap.put(nId3, workerServiceClientMock);

		// Mocking
		Future<Void> future = Mockito.mock(Future.class);
		when(workerServiceClientMock.deregisterObserver(anyObject())).thenReturn(future);
		when(future.get()).thenReturn(null);
		when(schedulerServiceClientMock.removeWorker(anyString())).thenReturn(future);

		nodeController.removeNode(nId2);

		assertFalse(workers.contains(nId2));
		assertFalse(workerInstanceMap.containsKey(nId2));
		assertFalse(workerInfoMap.containsKey(nId2));
		assertFalse(workerConnectionMap.containsKey(nId2));
		assertEquals(2, workerConnectionMap.size());
		assertEquals(2, workerInfoMap.size());
		assertEquals(2, workerInstanceMap.size());
		assertEquals(2, workers.size());
	}


	@Test(expected = UnknownNodeException.class)
	public void removeUnknownWorker() throws Exception {
		String wId = UUID.randomUUID().toString();
		nodeController.removeNode(wId);
	}


	@Test
	public void getWorkerServiceEndpoint() throws Exception {
		Map<String, ServiceEndpointDTO> endpoints = new HashMap<>();
		int nr = new Random().nextInt(5) + 5;
		WorkerServiceClientFactory factory = new WorkerServiceClientFactory();
		for (int i = 0; i < nr; i++) {
			String wId = UUID.randomUUID().toString();
			ServiceEndpointDTO endpoint = new ServiceEndpointDTO(UUID.randomUUID().toString(), i, Protocol.REST);
			endpoints.put(wId, endpoint);
			workers.add(wId);
			workerConnectionMap.put(wId, factory.createClient(endpoint));
			workerInfoMap.put(wId, null);
		}

		for (Map.Entry<String, ServiceEndpointDTO> e : endpoints.entrySet()) {
			ServiceEndpointDTO endpoint = nodeController.getNodeServiceEndpoint(e.getKey());
			assertEquals(e.getValue(), endpoint);
		}
	}


	@Test(expected = UnknownNodeException.class)
	public void getUnknownWorkerServiceEndpoint() throws Exception {
		String wId = UUID.randomUUID().toString();
		nodeController.getNodeServiceEndpoint(wId);
	}


	@Test
	public void getAndSetRoutines() throws Exception {
		assertNotNull(nodeController.getStoreRoutineId());

		String storeRoutineId = UUID.randomUUID().toString();
		String partitionRoutineId = UUID.randomUUID().toString();

		// Add 3 workers
		String worker1Id = UUID.randomUUID().toString();
		String worker2Id = UUID.randomUUID().toString();
		String worker3Id = UUID.randomUUID().toString();
		workerConnectionMap.put(worker1Id, workerServiceClientMock);
		workerConnectionMap.put(worker2Id, workerServiceClientMock);
		workerConnectionMap.put(worker3Id, workerServiceClientMock);

		// Mocking actions
		Future<Void> futureState = Mockito.mock(Future.class);
		when(workerServiceClientMock.setStoreRoutine(storeRoutineId)).thenReturn(futureState);
		when(futureState.get()).thenReturn(null);


		nodeController.setStoreRoutineId(storeRoutineId);
		assertEquals(storeRoutineId, nodeController.getStoreRoutineId());

		verify(workerServiceClientMock, times(3)).setStoreRoutine(storeRoutineId);
	}


	@Test
	public void getInstance() throws Exception {
		WorkerController wc = WorkerController.getInstance();
		assertEquals(0, wc.getAllNodeIds().size());

		WorkerController wc2 = WorkerController.getInstance();
		assertSame(wc, wc2);
	}

	@Test
	public void abortTask() throws Exception {

		// Add worker - tasks assignments
		String worker1Id = UUID.randomUUID().toString();
		String worker2Id = UUID.randomUUID().toString();
		String worker3Id = UUID.randomUUID().toString();

		String task1 = UUID.randomUUID().toString();
		String task2 = UUID.randomUUID().toString();
		String task3 = UUID.randomUUID().toString();
		String task4 = UUID.randomUUID().toString();
		String task5 = UUID.randomUUID().toString();
		String task6 = UUID.randomUUID().toString();

		Set<String> worker1Tasks = new LinkedHashSet<>();
		worker1Tasks.add(task1);
		worker1Tasks.add(task2);

		Set<String> worker2Tasks = new LinkedHashSet<>();
		worker2Tasks.add(task3);

		Set<String> worker3Tasks = new LinkedHashSet<>();
		worker3Tasks.add(task4);
		worker3Tasks.add(task5);
		worker3Tasks.add(task6);

		workerTaskAssignment.put(worker1Id, worker1Tasks);
		workerTaskAssignment.put(worker2Id, worker2Tasks);
		workerTaskAssignment.put(worker3Id, worker3Tasks);

		// Mocking actions
//		verify(nodeController, times(1)).getWorkerClient((String)notNull());
		// TODO: implement when code is finished
	}

	@Test
	public void findNodesForShutdownTest_running() {
		WorkerController wc = WorkerController.getInstance();
		String node1Id = UUID.randomUUID().toString();
		NodeInfoDTO nodeInfoDTO1 = new NodeInfoDTO();
		nodeInfoDTO1.setId(node1Id);
		nodeInfoDTO1.putToParameters("numberOfQueuedTasks", "1");
		String node2Id = UUID.randomUUID().toString();
		NodeInfoDTO nodeInfoDTO2 = new NodeInfoDTO();
		nodeInfoDTO2.setId(node2Id);
		nodeInfoDTO2.putToParameters("numberOfQueuedTasks", "3");
		String node3Id = UUID.randomUUID().toString();
		NodeInfoDTO nodeInfoDTO3 = new NodeInfoDTO();
		nodeInfoDTO3.setId(node3Id);
		nodeInfoDTO3.putToParameters("numberOfQueuedTasks", "2");
		String node4Id = UUID.randomUUID().toString();
		NodeInfoDTO nodeInfoDTO4 = new NodeInfoDTO();
		nodeInfoDTO4.setId(node4Id);
		nodeInfoDTO4.putToParameters("numberOfQueuedTasks", "4");
		int nrOfNodesToShutdown = 2;
		Map<String, NodeInfoDTO> nodesMap = new HashMap<>();
		nodesMap.put(node1Id, nodeInfoDTO1);
		nodesMap.put(node2Id, nodeInfoDTO2);
		nodesMap.put(node3Id, nodeInfoDTO3);
		nodesMap.put(node4Id, nodeInfoDTO4);
		wc.nodeInfoMap.clear();
		wc.nodeInfoMap.putAll(nodesMap);

		List<String> nodeIds = wc.findNodesForShutdown(nrOfNodesToShutdown);

		Assert.assertEquals(nrOfNodesToShutdown, nodeIds.size());
		Assert.assertTrue(nodeIds.contains(node1Id));
		Assert.assertFalse(nodeIds.contains(node2Id));
		Assert.assertTrue(nodeIds.contains(node3Id));
		Assert.assertFalse(nodeIds.contains(node4Id));
	}

	@Test (expected = IllegalArgumentException.class)
	public void findNodesForShutdownTest_wrongNumber() {
		WorkerController wc = WorkerController.getInstance();

		wc.findNodesForShutdown(10);
	}
}


