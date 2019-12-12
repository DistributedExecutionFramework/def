package at.enfilo.def.client.shell;

import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.cloud.communication.dto.SupportedCloudEnvironment;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.manager.api.IManagerServiceClient;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.shell.core.CommandResult;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;

import static at.enfilo.def.client.shell.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ManagerCommandsTest extends ShellBaseTest {

	@Test
	public void listCluster() throws Exception {
		IManagerServiceClient clientMock = setupMocks();
		List<String> cIds = new LinkedList<>();
		String cId = UUID.randomUUID().toString();
		cIds.add(cId);
		Future<List<String>> future = Mockito.mock(Future.class);
		when(clientMock.getClusterIds()).thenReturn(future);
		when(future.get()).thenReturn(cIds);

		CommandResult result = shell.executeCommand(
				String.format("%s", CMD_MANAGER_CLUSTER_LIST)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(cId));
	}


	@Test
	public void getClusterInfo() throws Exception {
		IManagerServiceClient clientMock = setupMocks();
		String cId = UUID.randomUUID().toString();
		ClusterInfoDTO clusterInfo = new ClusterInfoDTO();
		clusterInfo.setId(cId);
		Future<ClusterInfoDTO> future = Mockito.mock(Future.class);
		when(clientMock.getClusterInfo(cId)).thenReturn(future);
		when(future.get()).thenReturn(clusterInfo);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_MANAGER_CLUSTER_SHOW, OPT_CLUSTER_ID, cId)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(cId));
	}


	@Test
	public void getClusterInfoToObject() throws Exception {
		IManagerServiceClient clientMock = setupMocks();
		String cId = UUID.randomUUID().toString();
		ClusterInfoDTO clusterInfo = new ClusterInfoDTO();
		clusterInfo.setId(cId);
		Future<ClusterInfoDTO> future = Mockito.mock(Future.class);
		when(clientMock.getClusterInfo(cId)).thenReturn(future);
		when(future.get()).thenReturn(clusterInfo);
		String objectName = UUID.randomUUID().toString();

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_MANAGER_CLUSTER_SHOW, OPT_CLUSTER_ID, cId, OPT_TO_OBJECT, objectName)
		);
		assertTrue(result.isSuccess());
		objects.getObjectMap().containsKey(objectName);
		assertEquals(clusterInfo, objects.getObject(objectName, ClusterInfoDTO.class));
	}

	@Test
	public void getClusterEndpoint() throws Exception {
		IManagerServiceClient clientMock = setupMocks();
		String cId = UUID.randomUUID().toString();
		ServiceEndpointDTO serviceEndpoint = new ServiceEndpointDTO();
		serviceEndpoint.setHost(cId);
		Future<ServiceEndpointDTO> future = Mockito.mock(Future.class);
		when(clientMock.getClusterEndpoint(cId)).thenReturn(future);
		when(future.get()).thenReturn(serviceEndpoint);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_MANAGER_CLUSTER_ENDPOINT, OPT_CLUSTER_ID, cId)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(cId));
	}

	@Test
	public void getClusterEndpointToObject() throws Exception {
		IManagerServiceClient clientMock = setupMocks();
		String cId = UUID.randomUUID().toString();
		ServiceEndpointDTO serviceEndpointDTO = new ServiceEndpointDTO();
		serviceEndpointDTO.setHost(cId);
		Future<ServiceEndpointDTO> future = Mockito.mock(Future.class);
		when(clientMock.getClusterEndpoint(cId)).thenReturn(future);
		when(future.get()).thenReturn(serviceEndpointDTO);
		String objectName = UUID.randomUUID().toString();

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_MANAGER_CLUSTER_ENDPOINT, OPT_CLUSTER_ID, cId, OPT_TO_OBJECT, objectName)
		);
		assertTrue(result.isSuccess());
		objects.getObjectMap().containsKey(objectName);
		assertEquals(serviceEndpointDTO, objects.getObject(objectName, ServiceEndpointDTO.class));
	}


	@Test
	public void createCluster() throws Exception {

		IManagerServiceClient clientMock = setupMocks();
		Random rnd = new Random();
		int nrOfWorkers = rnd.nextInt();
		int nrOfReducers = rnd.nextInt();
		SupportedCloudEnvironment cloudEnvironment = SupportedCloudEnvironment.AWS;
		AWSSpecificationDTO specification = new AWSSpecificationDTO();
		String name = UUID.randomUUID().toString();
		objects.getObjectMap().put(name, specification);
		String id = UUID.randomUUID().toString();
		Future<String> futureId = Mockito.mock(Future.class);
		when(clientMock.createAWSCluster(nrOfWorkers, nrOfReducers, specification)).thenReturn(futureId);
		when(futureId.get()).thenReturn(id);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s --%s %s",
						CMD_MANAGER_CLUSTER_CREATE,
						OPT_NUMBER_OF_WORKERS, nrOfWorkers,
						OPT_NUMBER_OF_REDUCERS, nrOfReducers,
						OPT_CLOUD_ENVIRONMENT, cloudEnvironment,
						OPT_CLOUD_SPECIFICATION, name
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				String.format(MESSAGE_MANAGER_CLUSTER_CREATED, id),
				result.getResult().toString()
		);
	}

	@Test
	public void adjustNodePoolSize() throws Exception {

		IManagerServiceClient clientMock = setupMocks();
		Random rnd = new Random();
		String clusterId = UUID.randomUUID().toString();
		int newNodePoolSize = rnd.nextInt();
		NodeType nodeType = NodeType.WORKER;
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.adjustNodePoolSize(clusterId, newNodePoolSize, nodeType)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s --%s %s",
						CMD_MANAGER_CLUSTER_ADJUST_NODE_POOL_SIZE,
						OPT_CLUSTER_ID, clusterId,
						OPT_NEW_NODE_POOL_SIZE, newNodePoolSize,
						OPT_NODE_TYPE, nodeType
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_MANAGER_NODE_POOL_SIZE_ADJUSTED,
				result.getResult().toString()
		);
	}


	@Test
	public void addClusterDirect() throws Exception {
		IManagerServiceClient clientMock = setupMocks();
		Random rnd = new Random();
		String host = UUID.randomUUID().toString();
		int port = rnd.nextInt();
		Protocol proto = Protocol.REST;
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(host, port, proto);
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.addCluster(endpoint)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %d --%s %s", CMD_MANAGER_CLUSTER_ADD_DIRECT,
						OPT_HOST, host,
						OPT_PORT, port,
						OPT_PROTOCOL, proto
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_MANAGER_ADD_CLUSTER,
				result.getResult().toString()
		);
	}


	@Test
	public void addClusterEndpoint() throws Exception {
		IManagerServiceClient clientMock = setupMocks();
		Random rnd = new Random();
		String name = UUID.randomUUID().toString();
		String host = UUID.randomUUID().toString();
		int port = rnd.nextInt();
		Protocol proto = Protocol.REST;
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(host, port, proto);
		objects.getObjectMap().put(name, endpoint);
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.addCluster(endpoint)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_MANAGER_CLUSTER_ADD_ENDPOINT,
						OPT_SERVICE_ENDPOINT, name
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_MANAGER_ADD_CLUSTER,
				result.getResult().toString()
		);
	}

	@Test
	public void deleteCluster() throws Exception {
		IManagerServiceClient clientMock = setupMocks();
		String cId = UUID.randomUUID().toString();
		Future<Void> future = Mockito.mock(Future.class);
		when(clientMock.deleteCluster(cId)).thenReturn(future);
		when(future.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_MANAGER_CLUSTER_REMOVE,
						OPT_CLUSTER_ID, cId
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_MANAGER_REMOVE_CLUSTER,
				result.getResult().toString()
		);
	}

	private IManagerServiceClient setupMocks() throws ClientCreationException {
		changeToManagerContext();
		IManagerServiceClient clientMock = Mockito.mock(IManagerServiceClient.class);
		session.setManagerServiceClient(clientMock);
		return clientMock;
	}
}
