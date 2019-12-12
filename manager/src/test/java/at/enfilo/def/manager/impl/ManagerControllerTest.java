package at.enfilo.def.manager.impl;

import at.enfilo.def.cloud.communication.api.ICloudCommunicationServiceClient;
import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.cloud.communication.dto.InstanceTypeDTO;
import at.enfilo.def.cluster.api.ClusterServiceClientFactory;
import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.TakeControlException;
import at.enfilo.def.library.api.client.ILibraryAdminServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryAdminServiceClientFactory;
import at.enfilo.def.manager.impl.mocks.ManagerControllerMock;
import at.enfilo.def.manager.util.ProgramClusterRegistry;
import at.enfilo.def.node.api.INodeServiceClient;
import at.enfilo.def.transfer.dto.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class ManagerControllerTest {

	private ManagerController controller;
	private ClusterServiceClientFactory clusterServiceClientFactory;
	private ICloudCommunicationServiceClient cloudCommunicationServiceClient;
	private ILibraryAdminServiceClient libraryAdminServiceClient;
	private ProgramClusterRegistry programClusterRegistry;

	private List<String> allNodesList;

	@Before
	public void setUp() throws Exception {
		clusterServiceClientFactory = Mockito.mock(ClusterServiceClientFactory.class);
		programClusterRegistry = Mockito.mock(ProgramClusterRegistry.class);
		cloudCommunicationServiceClient = Mockito.mock(ICloudCommunicationServiceClient.class);
		libraryAdminServiceClient = Mockito.mock(ILibraryAdminServiceClient.class);

		Constructor<ManagerController> constructor = ManagerController.class.getDeclaredConstructor(
			ProgramClusterRegistry.class,
			ClusterServiceClientFactory.class,
			ILibraryAdminServiceClient.class,
			ICloudCommunicationServiceClient.class
		);
		constructor.setAccessible(true);
		controller = constructor.newInstance(
			programClusterRegistry,
			clusterServiceClientFactory,
			libraryAdminServiceClient,
			cloudCommunicationServiceClient
		);
		this.allNodesList = new LinkedList<>();
	}

	@Test
	public void getInstance() throws Exception {
		ManagerController instance1 = ManagerController.getInstance();
		ManagerController instance2 = ManagerController.getInstance();

		assertSame(instance1, instance2);
	}

	@Test
	public void getClusterInfo() throws Exception {
		String cId = UUID.randomUUID().toString();
		ClusterInfoDTO info = new ClusterInfoDTO();
		info.setId(cId);

		// Mocking
		IClusterServiceClient clusterServiceClient = Mockito.mock(IClusterServiceClient.class);
		when(programClusterRegistry.getClusterClient(cId)).thenReturn(clusterServiceClient);
		Future<ClusterInfoDTO> futureInfo = Mockito.mock(Future.class);
		when(clusterServiceClient.getClusterInfo()).thenReturn(futureInfo);
		when(futureInfo.get()).thenReturn(info);

		ClusterInfoDTO requestedInfo = controller.getClusterInfo(cId);
		assertEquals(info, requestedInfo);
	}


	@Test(expected = UnknownClusterException.class)
	public void getUnknownClusterInfo() throws Exception {
		String cId = UUID.randomUUID().toString();

		// Mocking
		when(programClusterRegistry.getClusterClient(cId)).thenThrow(UnknownClusterException.class);

		controller.getClusterInfo(cId);
	}

	@Test(expected = ClientCommunicationException.class)
	public void getClusterInfoClientCommunicationFailed() throws Exception {
		String cId = UUID.randomUUID().toString();

		// Mocking
		IClusterServiceClient clientMock = Mockito.mock(IClusterServiceClient.class);
		when(programClusterRegistry.getClusterClient(cId)).thenReturn(clientMock);
		when(clientMock.getClusterInfo()).thenThrow(ClientCommunicationException.class);

		controller.getClusterInfo(cId);
	}

	@Test
	public void getClusterEndpoint() throws Exception {
		String cId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		endpoint.setHost(cId);

		when(programClusterRegistry.getClusterEndpoint(cId)).thenReturn(endpoint);

		ServiceEndpointDTO requestedEndpoint = controller.getClusterEndpoint(cId);
		assertEquals(endpoint, requestedEndpoint);
	}

	@Test(expected = UnknownClusterException.class)
	public void getUnknownClusterEndpoint() throws Exception {
		String cId = UUID.randomUUID().toString();

		// Mocking
		when(programClusterRegistry.getClusterEndpoint(cId)).thenThrow(UnknownClusterException.class);

		controller.getClusterEndpoint(cId);
	}

	@Test
	public void createClusterTest() throws Exception {

		ManagerControllerMock managerControllerMock = new ManagerControllerMock(programClusterRegistry, clusterServiceClientFactory, cloudCommunicationServiceClient);
		managerControllerMock.setCallCreateCluster(true);
		managerControllerMock.setCallAddCluster(false);
		managerControllerMock.setCallBootNodeInstancesInCluster(false);

		Future<String> futureString = Mockito.mock(Future.class);
		IClusterServiceClient clusterServiceClient = Mockito.mock(IClusterServiceClient.class);
		Future<ServiceEndpointDTO> futureServiceEndpoint = Mockito.mock(Future.class);
		ServiceEndpointDTO serviceEndpointDTO = new ServiceEndpointDTO();
		serviceEndpointDTO.setHost("127.0.0.1");
		serviceEndpointDTO.setPort(1234);
		serviceEndpointDTO.setProtocol(Protocol.THRIFT_TCP);
		LibraryAdminServiceClientFactory libraryAdminServiceClientFactory = Mockito.mock(LibraryAdminServiceClientFactory.class);
		ILibraryAdminServiceClient libraryAdminServiceClient = Mockito.mock(ILibraryAdminServiceClient.class);

		when(futureString.get()).thenReturn(UUID.randomUUID().toString());
		when(futureServiceEndpoint.get()).thenReturn(serviceEndpointDTO);

		when(cloudCommunicationServiceClient.bootClusterInstance(anyString())).thenReturn(futureString);
		when(cloudCommunicationServiceClient.getPrivateIPAddressOfCloudInstance(anyString(), anyString())).thenReturn(futureString);
		when(clusterServiceClientFactory.createClient((ServiceEndpointDTO)notNull())).thenReturn(clusterServiceClient);
		when(cloudCommunicationServiceClient.mapDEFIdToCloudInstanceId(anyString(), anyString(), anyString())).thenReturn(null);
		when(clusterServiceClient.getLibraryEndpointConfiguration()).thenReturn(futureServiceEndpoint);
		when(libraryAdminServiceClientFactory.createClient((ServiceEndpointDTO)notNull())).thenReturn(libraryAdminServiceClient);
		when(libraryAdminServiceClient.setMasterLibrary((ServiceEndpointDTO)notNull())).thenReturn(null);

		managerControllerMock.createCluster(UUID.randomUUID().toString(), 2, 1, libraryAdminServiceClientFactory);

		verify(cloudCommunicationServiceClient, times(1)).bootClusterInstance(anyString());
		verify(cloudCommunicationServiceClient, times(1)).getPrivateIPAddressOfCloudInstance(anyString(), anyString());
		verify(clusterServiceClientFactory, times(1)).createClient((ServiceEndpointDTO)notNull());
		verify(cloudCommunicationServiceClient, times(1)).mapDEFIdToCloudInstanceId(anyString(), anyString(), anyString());
		verify(clusterServiceClient, times(1)).getLibraryEndpointConfiguration();
		verify(libraryAdminServiceClientFactory, times(1)).createClient((ServiceEndpointDTO)notNull());
		verify(libraryAdminServiceClient, times(1)).setMasterLibrary((ServiceEndpointDTO)notNull());
	}

	@Test
	public void createAWSClusterTest() throws Exception {
		ManagerControllerMock managerControllerMock = new ManagerControllerMock(
				programClusterRegistry,
				clusterServiceClientFactory,
				cloudCommunicationServiceClient);
		managerControllerMock.setCallCreateCluster(false);

		Future<String> futureString = Mockito.mock(Future.class);

		when(futureString.get()).thenReturn(UUID.randomUUID().toString());
		when(cloudCommunicationServiceClient.createAWSCluster((AWSSpecificationDTO)notNull())).thenReturn(futureString);

		managerControllerMock.createAWSCluster(2, 1, new AWSSpecificationDTO());

		verify(cloudCommunicationServiceClient, times(1)).createAWSCluster((AWSSpecificationDTO)notNull());
	}

	@Test
	public void adjustNodePoolSizeTest_bootNodes() throws Exception {
		int oldWorkerPoolSize = 2;
		int newWorkerPoolSize = 4;
		NodeType nodeType = NodeType.WORKER;
		ManagerControllerMock managerControllerMock = new ManagerControllerMock(
				programClusterRegistry,
				clusterServiceClientFactory,
				cloudCommunicationServiceClient
		);
		managerControllerMock.setCallGetCloudClusterId(false);
		managerControllerMock.setCallGetClusterInfo(false);
		managerControllerMock.setNumberOfWorkersInClusterInfoDTO(oldWorkerPoolSize);
		managerControllerMock.setCallGetClusterEndpoint(false);
		managerControllerMock.setCallBootNodeInstancesInCluster(false);
		ClusterServiceClientFactory clusterServiceClientFactory = Mockito.mock(ClusterServiceClientFactory.class);
		IClusterServiceClient clusterServiceClient = Mockito.mock(IClusterServiceClient.class);
		Future<ServiceEndpointDTO> futureServiceEndpoint = Mockito.mock(Future.class);
		ServiceEndpointDTO serviceEndpointDTO = new ServiceEndpointDTO();

		when(clusterServiceClientFactory.createClient((ServiceEndpointDTO)notNull())).thenReturn(clusterServiceClient);
		when(clusterServiceClient.getLibraryEndpointConfiguration()).thenReturn(futureServiceEndpoint);
		when(futureServiceEndpoint.get()).thenReturn(serviceEndpointDTO);

		int counterBootNodeInstancesInClusterCalls = managerControllerMock.counterBootNodeInstancesInClusterCalls;

		managerControllerMock.adjustNodePoolSize(UUID.randomUUID().toString(), newWorkerPoolSize, nodeType, clusterServiceClientFactory);

		verify(clusterServiceClientFactory, times(1)).createClient((ServiceEndpointDTO)notNull());
		verify(clusterServiceClient, times(1)).getLibraryEndpointConfiguration();
		Assert.assertEquals(counterBootNodeInstancesInClusterCalls + 1, managerControllerMock.counterBootNodeInstancesInClusterCalls);
		verify(cloudCommunicationServiceClient, times(0)).terminateNodes(anyString(), anyList());
		verify(clusterServiceClient, times(0)).findNodesForShutdown((NodeType)notNull(), anyInt());
		verify(clusterServiceClient, times(0)).removeNode(anyString());
	}

	@Test
	public void adjustNodePoolSizeTest_terminateNodes() throws Exception {
		int oldWorkerPoolSize = 4;
		int newWorkerPoolSize = 2;
		NodeType nodeType = NodeType.WORKER;
		ManagerControllerMock managerControllerMock = new ManagerControllerMock(
				programClusterRegistry,
				clusterServiceClientFactory,
				cloudCommunicationServiceClient
		);
		managerControllerMock.setCallGetCloudClusterId(false);
		managerControllerMock.setCallGetClusterInfo(false);
		managerControllerMock.setNumberOfWorkersInClusterInfoDTO(oldWorkerPoolSize);
		managerControllerMock.setCallGetClusterEndpoint(false);
		managerControllerMock.setCallBootNodeInstancesInCluster(false);
		ClusterServiceClientFactory clusterServiceClientFactory = Mockito.mock(ClusterServiceClientFactory.class);
		IClusterServiceClient clusterServiceClient = Mockito.mock(IClusterServiceClient.class);
		Future<ServiceEndpointDTO> futureServiceEndpoint = Mockito.mock(Future.class);
		ServiceEndpointDTO serviceEndpointDTO = new ServiceEndpointDTO();
		Future<List<String>> futureList = Mockito.mock(Future.class);
		List<String> nodeIds = new LinkedList<>();
		for (int i = 0; i < oldWorkerPoolSize - newWorkerPoolSize; i++) {
			nodeIds.add(UUID.randomUUID().toString());
		}

		when(clusterServiceClientFactory.createClient((ServiceEndpointDTO)notNull())).thenReturn(clusterServiceClient);
		when(clusterServiceClient.getLibraryEndpointConfiguration()).thenReturn(futureServiceEndpoint);
		when(futureServiceEndpoint.get()).thenReturn(serviceEndpointDTO);
		when(clusterServiceClient.findNodesForShutdown((NodeType)notNull(), anyInt())).thenReturn(futureList);
		when(futureList.get()).thenReturn(nodeIds);

		int counterBootNodeInstancesInClusterCalls = managerControllerMock.counterBootNodeInstancesInClusterCalls;

		managerControllerMock.adjustNodePoolSize(UUID.randomUUID().toString(), newWorkerPoolSize, nodeType, clusterServiceClientFactory);

		verify(clusterServiceClientFactory, times(1)).createClient((ServiceEndpointDTO)notNull());
		verify(clusterServiceClient, times(1)).getLibraryEndpointConfiguration();
		Assert.assertEquals(counterBootNodeInstancesInClusterCalls, managerControllerMock.counterBootNodeInstancesInClusterCalls);
		verify(cloudCommunicationServiceClient, times(1)).terminateNodes(anyString(), anyList());
		verify(clusterServiceClient, times(1)).findNodesForShutdown((NodeType)notNull(), anyInt());
		verify(clusterServiceClient, times(oldWorkerPoolSize - newWorkerPoolSize)).removeNode(anyString());
	}

	@Test
	public void adjustNodePoolSizeTest_remainNodePoolSize() throws Exception {
		int oldWorkerPoolSize = 2;
		int newWorkerPoolSize = 2;
		NodeType nodeType = NodeType.WORKER;
		ManagerControllerMock managerControllerMock = new ManagerControllerMock(
				programClusterRegistry,
				clusterServiceClientFactory,
				cloudCommunicationServiceClient
		);
		managerControllerMock.setCallGetCloudClusterId(false);
		managerControllerMock.setCallGetClusterInfo(false);
		managerControllerMock.setNumberOfWorkersInClusterInfoDTO(oldWorkerPoolSize);
		managerControllerMock.setCallGetClusterEndpoint(false);
		ClusterServiceClientFactory clusterServiceClientFactory = Mockito.mock(ClusterServiceClientFactory.class);
		IClusterServiceClient clusterServiceClient = Mockito.mock(IClusterServiceClient.class);
		Future<ServiceEndpointDTO> futureServiceEndpoint = Mockito.mock(Future.class);
		ServiceEndpointDTO serviceEndpointDTO = new ServiceEndpointDTO();

		when(clusterServiceClientFactory.createClient((ServiceEndpointDTO)notNull())).thenReturn(clusterServiceClient);
		when(clusterServiceClient.getLibraryEndpointConfiguration()).thenReturn(futureServiceEndpoint);
		when(futureServiceEndpoint.get()).thenReturn(serviceEndpointDTO);

		int counterBootNodeInstancesInClusterCalls = managerControllerMock.counterBootNodeInstancesInClusterCalls;

		managerControllerMock.adjustNodePoolSize(UUID.randomUUID().toString(), newWorkerPoolSize, nodeType, clusterServiceClientFactory);

		verify(clusterServiceClientFactory, times(1)).createClient((ServiceEndpointDTO)notNull());
		verify(clusterServiceClient, times(1)).getLibraryEndpointConfiguration();
		Assert.assertEquals(counterBootNodeInstancesInClusterCalls, managerControllerMock.counterBootNodeInstancesInClusterCalls);
		verify(cloudCommunicationServiceClient, times(0)).terminateNodes(anyString(), anyList());
		verify(clusterServiceClient, times(0)).findNodesForShutdown((NodeType)notNull(), anyInt());
		verify(clusterServiceClient, times(0)).removeNode(anyString());
	}

	@Test
	public void bootNodeInstancesInClusterTest() throws Exception {
		this.allNodesList = new LinkedList<>();
		int nrOfNodeIs = 2;
		List<String> nodeIds = new LinkedList<>();
		for (int i = 0; i < nrOfNodeIs; i++) {
			nodeIds.add(UUID.randomUUID().toString());
		}
		Future<List<String>> futureNodeIds = Mockito.mock(Future.class);
		Future<String> futurePrivateIPAddress = Mockito.mock(Future.class);
		IClusterServiceClient clusterServiceClient = Mockito.mock(IClusterServiceClient.class);
		Future<ServiceEndpointDTO> futureNodeServiceEndpoint = Mockito.mock(Future.class);
		ServiceEndpointDTO serviceEndpointDTO = new ServiceEndpointDTO();
		serviceEndpointDTO.setHost("127.0.0.1");
		serviceEndpointDTO.setProtocol(Protocol.THRIFT_TCP);
		serviceEndpointDTO.setPort(1234);
		INodeServiceClient nodeServiceClient = Mockito.mock(INodeServiceClient.class);
		Future<NodeInfoDTO> futureNodeInfo = Mockito.mock(Future.class);
		NodeInfoDTO nodeInfoDTO = new NodeInfoDTO();
		nodeInfoDTO.setId(UUID.randomUUID().toString());
		LibraryAdminServiceClientFactory libraryAdminServiceClientFactory = Mockito.mock(LibraryAdminServiceClientFactory.class);
		ILibraryAdminServiceClient libraryAdminServiceClient = Mockito.mock(ILibraryAdminServiceClient.class);
		Future<List<String>> futureAllNodes = Mockito.mock(Future.class);

		when(futureNodeIds.get()).thenReturn(nodeIds);
		when(futurePrivateIPAddress.get()).thenReturn(UUID.randomUUID().toString());
		when(futureNodeServiceEndpoint.get()).thenReturn(serviceEndpointDTO);
		when(futureNodeInfo.get()).thenReturn(nodeInfoDTO);
		when(futureAllNodes.get()).then(invocation -> {
			prepareAllNodesList();
			List<String> list = new LinkedList<>(this.allNodesList);
			return list;
		});
		when(cloudCommunicationServiceClient.bootNodes(anyString(), (InstanceTypeDTO)notNull(), anyInt())).thenReturn(futureNodeIds);
		when(cloudCommunicationServiceClient.mapDEFIdToCloudInstanceId(anyString(), anyString(), anyString())).thenReturn(null);
		when(cloudCommunicationServiceClient.getPrivateIPAddressOfCloudInstance(anyString(), anyString())).thenReturn(futurePrivateIPAddress);
		when(clusterServiceClient.getNodeServiceEndpointConfiguration((NodeType)notNull())).thenReturn(futureNodeServiceEndpoint);
		when(clusterServiceClient.getLibraryEndpointConfiguration()).thenReturn(futureNodeServiceEndpoint);
		when(clusterServiceClient.addNode((ServiceEndpointDTO)notNull(), (NodeType)notNull())).thenReturn(null);
		when(clusterServiceClient.getAllNodes((NodeType)notNull())).thenReturn(futureAllNodes);
		when(nodeServiceClient.getInfo()).thenReturn(futureNodeInfo);
		when(libraryAdminServiceClientFactory.createClient((ServiceEndpointDTO)notNull())).thenReturn(libraryAdminServiceClient);
		when(libraryAdminServiceClient.setMasterLibrary((ServiceEndpointDTO)notNull())).thenReturn(null);

		controller.bootNodeInstancesInCluster(UUID.randomUUID().toString(), clusterServiceClient, serviceEndpointDTO, libraryAdminServiceClientFactory, InstanceTypeDTO.WORKER, nrOfNodeIs);

		verify(cloudCommunicationServiceClient, times(1)).bootNodes(anyString(), (InstanceTypeDTO)notNull(), anyInt());
		verify(cloudCommunicationServiceClient, times(nrOfNodeIs)).mapDEFIdToCloudInstanceId(anyString(), anyString(), anyString());
		verify(cloudCommunicationServiceClient, times(nrOfNodeIs)).getPrivateIPAddressOfCloudInstance(anyString(), anyString());
		verify(clusterServiceClient, times(1)).getNodeServiceEndpointConfiguration((NodeType)notNull());
		verify(clusterServiceClient, times(1)).getLibraryEndpointConfiguration();
		verify(clusterServiceClient, times(nrOfNodeIs)).addNode((ServiceEndpointDTO)notNull(), (NodeType)notNull());
		verify(libraryAdminServiceClientFactory, times(nrOfNodeIs)).createClient((ServiceEndpointDTO)notNull());
	}

	@Test
	public void addCluster() throws Exception {
		String cId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		ClusterInfoDTO info = new ClusterInfoDTO();
		info.setId(cId);

		// Mocking
		IClusterServiceClient clusterServiceClient = Mockito.mock(IClusterServiceClient.class);
		when(clusterServiceClientFactory.createClient(endpoint)).thenReturn(clusterServiceClient);
		Future<ClusterInfoDTO> futureInfo = Mockito.mock(Future.class);
		when(clusterServiceClient.getClusterInfo()).thenReturn(futureInfo);
		when(futureInfo.get()).thenReturn(info);
		Future<Void> futureTakeControl = Mockito.mock(Future.class);
		when(clusterServiceClient.takeControl(controller.getManagerId())).thenReturn(futureTakeControl);
		when(futureTakeControl.get()).thenReturn(null);

		controller.addCluster(endpoint);

		verify(programClusterRegistry).addCluster(eq(cId), any());
	}


	@Test(expected = TakeControlException.class)
	public void addClusterTakeControl() throws Exception {
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		ClusterInfoDTO info = new ClusterInfoDTO();
		info.setId(UUID.randomUUID().toString());

		// Mocking
		IClusterServiceClient clusterServiceClient = Mockito.mock(IClusterServiceClient.class);
		when(clusterServiceClientFactory.createClient(endpoint)).thenReturn(clusterServiceClient);
		Future<ClusterInfoDTO> futureInfo = Mockito.mock(Future.class);
		when(clusterServiceClient.getClusterInfo()).thenReturn(futureInfo);
		when(futureInfo.get()).thenReturn(info);
		Future<Void> futureTakeControl = Mockito.mock(Future.class);
		when(clusterServiceClient.takeControl(controller.getManagerId())).thenReturn(futureTakeControl);
		when(futureTakeControl.get()).thenThrow(new ExecutionException("failed", null));

		controller.addCluster(endpoint);
	}

	@Test
	public void destroyCluster() throws Exception {
		String cId = UUID.randomUUID().toString();
		controller.mapClusterIdToCloudClusterId(cId, UUID.randomUUID().toString());

		// Mocking
		IClusterServiceClient clusterServiceClient = Mockito.mock(IClusterServiceClient.class);
		when(programClusterRegistry.getClusterClient(cId)).thenReturn(clusterServiceClient);
		when(cloudCommunicationServiceClient.shutdownCloudCluster((String)notNull())).thenReturn(null);

		controller.destroyCluster(cId);

		verify(clusterServiceClient).destroyCluster();
		verify(cloudCommunicationServiceClient, times(1)).shutdownCloudCluster((String)notNull());
	}

	@Test (expected = ClientCommunicationException.class)
	public void destroyClusterClientCommunicationError() throws Exception {
		String cId = UUID.randomUUID().toString();

		// Mocking
		IClusterServiceClient clusterServiceClient = Mockito.mock(IClusterServiceClient.class);
		when(programClusterRegistry.getClusterClient(cId)).thenReturn(clusterServiceClient);
		doThrow(ClientCommunicationException.class).when(clusterServiceClient).destroyCluster();

		controller.destroyCluster(cId);
	}


	@Test (expected = UnknownClusterException.class)
	public void destroyUnknownCluster() throws Exception {
		String cId = UUID.randomUUID().toString();

		// Mocking
		when(programClusterRegistry.getClusterClient(cId)).thenThrow(UnknownClusterException.class);

		controller.destroyCluster(cId);
	}


	@Test
	public void getManagerId() throws Exception {
		String id1 = controller.getManagerId();
		String id2 = controller.getManagerId();

		assertEquals(id1, id2);
	}

	@Test
	public void getClusterIds() throws Exception {
		controller.getClusterIds();

		verify(programClusterRegistry).getClusterIds();
	}

	@Test
	public void createClientRoutine() throws Exception {
		String name = UUID.randomUUID().toString();
		String expectedRoutineId = UUID.randomUUID().toString();
		FeatureDTO javaFeature = new FeatureDTO();
		javaFeature.setGroup("language");
		javaFeature.setName("java");
		javaFeature.setVersion("1.8");
		List<FeatureDTO> requiredFeatures = Arrays.asList(javaFeature);
		List<String> arguments = Arrays.asList("argument");
		RoutineDTO routine = new RoutineDTO();
		routine.setName(name);
		routine.setRequiredFeatures(requiredFeatures);
		routine.setArguments(arguments);

		Future<String> frId = Mockito.mock(Future.class);
		when(frId.get()).thenReturn(expectedRoutineId);
		when(libraryAdminServiceClient.createRoutine(any())).thenReturn(frId);

		String rId = controller.createClientRoutine(routine);

		assertEquals(expectedRoutineId, rId);
	}

	@Test
	public void createClientRoutineBinary() throws Exception {
		String rId = UUID.randomUUID().toString();
		String rbName = "name";
		Random rnd = new Random();
		long sizeInBytes = rnd.nextInt();
		String md5 = UUID.randomUUID().toString();
		boolean isPrimary = rnd.nextBoolean();
		String rbId = UUID.randomUUID().toString();

		Future<String> frbId = Mockito.mock(Future.class);
		when(frbId.get()).thenReturn(rbId);
		when(libraryAdminServiceClient.createRoutineBinary(rId, rbName, md5, sizeInBytes, isPrimary)).thenReturn(frbId);

		String routineBinaryId = controller.createClientRoutineBinary(rId, rbName, md5, sizeInBytes, isPrimary);

		assertEquals(rbId, routineBinaryId);
	}

	@Test
	public void uploadClientRoutineBinaryChunk() throws Exception {
		String rbId = UUID.randomUUID().toString();
		Random rnd = new Random();
		RoutineBinaryChunkDTO chunk = new RoutineBinaryChunkDTO();
		chunk.setChunkSize(rnd.nextInt());
		chunk.setChunk((short)rnd.nextInt());
		chunk.setTotalChunks((short)rnd.nextInt());

		Future<Void> future = Mockito.mock(Future.class);
		when(future.get()).thenReturn(null);
		when(libraryAdminServiceClient.uploadRoutineBinaryChunk(rbId, chunk)).thenReturn(future);

		controller.uploadClientRoutineBinaryChunk(rbId, chunk);

		verify(libraryAdminServiceClient).uploadRoutineBinaryChunk(rbId, chunk);
	}

	@Test
	public void removeClientRoutine() throws Exception {
		String rId = UUID.randomUUID().toString();

		Future<Void> futureRemoveRoutine = Mockito.mock(Future.class);
		when(futureRemoveRoutine.get()).thenReturn(null);
		when(libraryAdminServiceClient.removeRoutine(rId)).thenReturn(futureRemoveRoutine);

		controller.removeClientRoutine(rId);

		verify(libraryAdminServiceClient).removeRoutine(rId);
	}

	private void prepareAllNodesList() {
		this.allNodesList.add(UUID.randomUUID().toString());
	}
}
