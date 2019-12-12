package at.enfilo.def.client.shell;

import at.enfilo.def.cluster.api.IClusterServiceClient;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
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
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClusterCommandsTest extends ShellBaseTest {


	@Test
	public void takeControl() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		String managerId = UUID.randomUUID().toString();
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.takeControl(managerId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
			String.format("%s --%s %s", CMD_CLUSTER_TAKE_CONTROL,
					OPT_MANAGER_ID, managerId
			)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_CLUSTER_TAKE_CONTROL,
				result.getResult().toString()
		);
	}

	@Test
	public void getInfo() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		ClusterInfoDTO info = new ClusterInfoDTO();
		String id = UUID.randomUUID().toString();
		info.setId(id);
		Future<ClusterInfoDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getClusterInfo()).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(info);

		CommandResult result = shell.executeCommand(CMD_CLUSTER_SHOW);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(id));
	}

	@Test
	public void getInfoToObject() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		String name = UUID.randomUUID().toString();
		ClusterInfoDTO info = new ClusterInfoDTO();
		String id = UUID.randomUUID().toString();
		info.setId(id);
		Future<ClusterInfoDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getClusterInfo()).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(info);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_CLUSTER_SHOW,
						OPT_TO_OBJECT, name
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(info, objects.getObjectMap().get(name));
	}



	@Test
	public void destroyCluster() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		CommandResult result = shell.executeCommand(CMD_CLUSTER_DESTROY);
		assertTrue(result.isSuccess());
		assertEquals(MESSAGE_CLUSTER_DESTROYED, result.getResult().toString());

		verify(clientMock).destroyCluster();
	}


	@Test
	public void getAllNodes() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		List<String> nIds = new LinkedList<>();
		String id = UUID.randomUUID().toString();
		NodeType type = NodeType.WORKER;
		nIds.add(id);
		Future<List<String>> futureMock = Mockito.mock(Future.class);
		when(clientMock.getAllNodes(type)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(nIds);

		CommandResult result = shell.executeCommand(
				String.format(
					"%s --%s %s", CMD_CLUSTER_NODE_LIST,
					OPT_NODE_TYPE, type
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(id));
	}


	@Test
	public void getNodeInfo() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		NodeInfoDTO info = new NodeInfoDTO();
		String id = UUID.randomUUID().toString();
		info.setId(id);
		Future<NodeInfoDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getNodeInfo(id)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(info);

		CommandResult result = shell.executeCommand(
				String.format(
						"%s --%s %s", CMD_CLUSTER_NODE_SHOW,
						OPT_NODE_ID, id
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(id));
	}


	@Test
	public void getNodeInfoToObject() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		String name = UUID.randomUUID().toString();
		NodeInfoDTO info = new NodeInfoDTO();
		String id = UUID.randomUUID().toString();
		info.setId(id);
		Future<NodeInfoDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getNodeInfo(id)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(info);

		CommandResult result = shell.executeCommand(
				String.format(
						"%s --%s %s --%s %s", CMD_CLUSTER_NODE_SHOW,
						OPT_NODE_ID, id,
						OPT_TO_OBJECT, name
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(info, objects.getObjectMap().get(name));
	}


	@Test
	public void addNodeDirect() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		Random rnd = new Random();
		NodeType type = NodeType.REDUCER;
		String host = UUID.randomUUID().toString();
		Integer port = rnd.nextInt();
		Protocol protocol = Protocol.THRIFT_TCP;
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(host, port, protocol);
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.addNode(endpoint, type)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
			String.format(
					"%s --%s %s --%s %s --%s %d --%s %s", CMD_CLUSTER_NODE_ADD_DIRECT,
					OPT_NODE_TYPE, type,
					OPT_HOST, host,
					OPT_PORT, port,
					OPT_PROTOCOL, protocol
			)
		);
		assertTrue(result.isSuccess());
		assertEquals(
			MESSAGE_CLUSTER_ADD_NODE, result.getResult().toString()
		);
	}

	@Test
	public void addNodeEndpoint() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		String name = UUID.randomUUID().toString();
		NodeType type = NodeType.WORKER;
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		objects.getObjectMap().put(name, endpoint);

		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.addNode(endpoint, type)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format(
						"%s --%s %s --%s %s", CMD_CLUSTER_NODE_ADD_ENDPOINT,
						OPT_NODE_TYPE, type,
						OPT_SERVICE_ENDPOINT,name
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_CLUSTER_ADD_NODE, result.getResult().toString()
		);
	}


	@Test
	public void removeNode() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		String nId = UUID.randomUUID().toString();
		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.removeNode(nId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_CLUSTER_NODE_REMOVE, OPT_NODE_ID, nId)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_CLUSTER_REMOVE_NODE, result.getResult().toString()
		);
	}


	@Test
	public void getNodeServiceEndpoint() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		String nId = UUID.randomUUID().toString();
		String host = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(host, new Random().nextInt(), Protocol.REST);
		Future<ServiceEndpointDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getNodeServiceEndpoint(nId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(endpoint);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_CLUSTER_NODE_GET_ENDPOINT, OPT_NODE_ID, nId)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(host));
	}


	@Test
	public void getNodeServiceEndpointToObject() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		String nId = UUID.randomUUID().toString();
		String object = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(UUID.randomUUID().toString(), new Random().nextInt(), Protocol.REST);
		Future<ServiceEndpointDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getNodeServiceEndpoint(nId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(endpoint);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s", CMD_CLUSTER_NODE_GET_ENDPOINT,
						OPT_NODE_ID, nId,
						OPT_TO_OBJECT, object)
		);
		assertTrue(result.isSuccess());
		assertEquals(String.format(MESSAGE_OBJECT_STORED, object), result.getResult());
		assertEquals(endpoint, objects.getObject(object, ServiceEndpointDTO.class));
	}


	@Test
	public void getSchedulerServiceEndpoint() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		String host = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(host, new Random().nextInt(), Protocol.REST);
		Future<ServiceEndpointDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getSchedulerServiceEndpoint(anyObject())).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(endpoint);

		CommandResult result = shell.executeCommand(String.format(
			"%s --%s %s",
			CMD_CLUSTER_SCHEDULER_GET_ENDPOINT,
				OPT_NODE_TYPE,
			NodeType.WORKER
		));
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(host));
	}


	@Test
	public void getSchedulerServiceEndpointToObject() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		String object = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(UUID.randomUUID().toString(), new Random().nextInt(), Protocol.REST);
		Future<ServiceEndpointDTO> futureMock = Mockito.mock(Future.class);
		when(clientMock.getSchedulerServiceEndpoint(anyObject())).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(endpoint);

		CommandResult result = shell.executeCommand(String.format(
			"%s --%s %s --%s %s",
			CMD_CLUSTER_SCHEDULER_GET_ENDPOINT,
				OPT_NODE_TYPE,
			NodeType.WORKER,
			OPT_TO_OBJECT,
			object
		));

		assertTrue(result.isSuccess());
		assertEquals(String.format(MESSAGE_OBJECT_STORED, object), result.getResult());
		assertEquals(endpoint, objects.getObject(object, ServiceEndpointDTO.class));
	}


	@Test
	public void setReducerSchedulerServiceEndpoint() throws Exception {
		setSchedulerServiceEndpoint(NodeType.REDUCER);
	}

	@Test
	public void setWorkerSchedulerServiceEndpoint() throws Exception {
		setSchedulerServiceEndpoint(NodeType.WORKER);
	}

	public void setSchedulerServiceEndpoint(NodeType nodeType) throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		String name = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		objects.getObjectMap().put(name, endpoint);

		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.setSchedulerServiceEndpoint(nodeType, endpoint)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(String.format(
			"%s --%s %s --%s %s",
			CMD_CLUSTER_SCHEDULER_SET_ENDPOINT,
				OPT_NODE_TYPE,
			nodeType,
			OPT_SERVICE_ENDPOINT,
			name
		));
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_CLUSTER_SET_SCHEDULER, result.getResult().toString()
		);
	}

	@Test
	public void setDefaultMapRoutine() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		String rId = UUID.randomUUID().toString();

		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.setDefaultMapRoutine(rId)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s", CMD_CLUSTER_ROUTINE_SET_MAP, OPT_ROUTINE_ID, rId)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_CLUSTER_ROUTINE_SET_MAP, result.getResult().toString()
		);
	}

	@Test
	public void setStoreRoutineWorker() throws Exception {
		IClusterServiceClient clientMock = setupMocks();

		String rId = UUID.randomUUID().toString();
		NodeType nodeType = NodeType.WORKER;

		Future<Void> futureMock = Mockito.mock(Future.class);
		when(clientMock.setStoreRoutine(rId, nodeType)).thenReturn(futureMock);
		when(futureMock.get()).thenReturn(null);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s",
						CMD_CLUSTER_ROUTINE_SET_STORE,
						OPT_NODE_TYPE,
						nodeType,
						OPT_ROUTINE_ID,
						rId)
		);
		assertTrue(result.isSuccess());
		assertEquals(
				MESSAGE_CLUSTER_ROUTINE_SET_STORE, result.getResult().toString()
		);
	}

	private IClusterServiceClient setupMocks() throws ClientCreationException {
		changeToClusterContext();
		IClusterServiceClient clientMock = Mockito.mock(IClusterServiceClient.class);
		session.setClusterServiceClient(clientMock);
		return clientMock;
	}
}
