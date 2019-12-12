package at.enfilo.def.manager.api;

import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.manager.api.rest.IManagerResponseService;
import at.enfilo.def.manager.api.rest.IManagerService;
import at.enfilo.def.transfer.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class ManagerServiceClientTest {
	private IManagerServiceClient client;
	private IManagerService managerServiceMock;
	private IManagerResponseService managerResponseServiceMock;
	private TicketService.Iface ticketServiceMock;

	@Before
	public void setUp() throws Exception {
		managerServiceMock = Mockito.mock(IManagerService.class);
		managerResponseServiceMock = Mockito.mock(IManagerResponseService.class);
		ticketServiceMock = Mockito.mock(TicketService.Iface.class);

		client = new ManagerServiceClientFactory().createDirectClient(
				managerServiceMock,
				managerResponseServiceMock,
				ticketServiceMock,
				IManagerServiceClient.class
		);
	}

	@Test
	public void getClusterIds() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		List<String> clusterIds = new LinkedList<>();
		clusterIds.add(UUID.randomUUID().toString());
		clusterIds.add(UUID.randomUUID().toString());

		when(managerServiceMock.getClusterIds()).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(managerResponseServiceMock.getClusterIds(ticketId)).thenReturn(clusterIds);

		Future<List<String>> futureClusterIds = client.getClusterIds();
		assertEquals(clusterIds, futureClusterIds.get());
	}

	@Test
	public void getClusterInfo() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String cId = UUID.randomUUID().toString();
		ClusterInfoDTO info = new ClusterInfoDTO();
		info.setId(cId);
		info.setName("cluster1");

		when(managerServiceMock.getClusterInfo(cId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(managerResponseServiceMock.getClusterInfo(ticketId)).thenReturn(info);

		Future<ClusterInfoDTO> futureInfo = client.getClusterInfo(cId);
		assertEquals(info, futureInfo.get());
	}

	@Test
	public void getClusterEndpoint() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String cId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		endpoint.setHost(UUID.randomUUID().toString());
		endpoint.setPort(new Random().nextInt());
		endpoint.setProtocol(Protocol.REST);
		endpoint.setPathPrefix(UUID.randomUUID().toString());

		when(managerServiceMock.getClusterEndpoint(cId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(managerResponseServiceMock.getClusterEndpoint(ticketId)).thenReturn(endpoint);

		Future<ServiceEndpointDTO> futureInfo = client.getClusterEndpoint(cId);
		assertEquals(endpoint, futureInfo.get());
	}

	@Test
	public void createAWSCluster() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String clusterId = UUID.randomUUID().toString();
		Random rnd = new Random();
		int nrOfWorkers = rnd.nextInt();
		int nrOfReducers = rnd.nextInt();
		AWSSpecificationDTO specification = new AWSSpecificationDTO();

		when(managerServiceMock.createAWSCluster(nrOfWorkers, nrOfReducers, specification)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(managerResponseServiceMock.createAWSCluster(ticketId)).thenReturn(clusterId);

		Future<String> futureId = client.createAWSCluster(nrOfWorkers, nrOfReducers, specification);
		assertEquals(clusterId, futureId.get());
	}

	@Test
	public void deleteCluster() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String cId = UUID.randomUUID().toString();

		when(managerServiceMock.destroyCluster(cId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureDeleteCluster = client.deleteCluster(cId);
		await().atMost(10, TimeUnit.SECONDS).until(futureDeleteCluster::isDone);
	}

	@Test
	public void addCluster() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpointDTO = new ServiceEndpointDTO();

		when(managerServiceMock.addCluster(endpointDTO)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureAddCluster = client.addCluster(endpointDTO);
		await().atMost(10, TimeUnit.SECONDS).until(futureAddCluster::isDone);
	}

	@Test
	public void adjustNodePoolSize() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String cId = UUID.randomUUID().toString();
		Random rnd = new Random();
		int newNodePoolSize = rnd.nextInt();
		NodeType nodeType = NodeType.WORKER;

		when(managerServiceMock.adjustNodePoolSize(cId, newNodePoolSize, nodeType)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureResult = client.adjustNodePoolSize(cId, newNodePoolSize, nodeType);
		await().atMost(10, TimeUnit.SECONDS).until(futureResult::isDone);
	}

	@Test
	public void createClientRoutine() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String name = UUID.randomUUID().toString();
		List<FeatureDTO> requiredFeatures = Arrays.asList(new FeatureDTO());
		String rId = UUID.randomUUID().toString();
		List<String> arguments = Arrays.asList("argument");
		RoutineDTO routine = new RoutineDTO();
		routine.setName(name);
		routine.setRequiredFeatures(requiredFeatures);
		routine.setArguments(arguments);

		when(managerServiceMock.createClientRoutine(routine)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(managerResponseServiceMock.createClientRoutine(ticketId)).thenReturn(rId);

		Future<String> futureResult = client.createClientRoutine(routine);
		assertEquals(rId, futureResult.get());
	}

	@Test
	public void createClientRoutineBinary() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();
		String rbId = UUID.randomUUID().toString();
		String rbName = "name";
		Random rnd = new Random();
		long sizeInBytes = rnd.nextInt();
		String md5 = UUID.randomUUID().toString();
		boolean isPrimary = rnd.nextBoolean();

		when(managerServiceMock.createClientRoutineBinary(rId, rbName, md5, sizeInBytes, isPrimary)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(managerResponseServiceMock.createClientRoutineBinary(ticketId)).thenReturn(rbId);

		Future<String> futureResult = client.createClientRoutineBinary(rId, rbName, md5, sizeInBytes, isPrimary);
		assertEquals(rbId, futureResult.get());
	}
	@Test
	public void uploadClientRoutineBinaryChunk() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String rbId = UUID.randomUUID().toString();
		Random rnd = new Random();
		RoutineBinaryChunkDTO chunkDTO = new RoutineBinaryChunkDTO();
		chunkDTO.setChunk((short)rnd.nextInt());
		chunkDTO.setChunkSize(rnd.nextInt());
		chunkDTO.setTotalChunks((short)rnd.nextInt());
		byte[] data = new byte[8];
		rnd.nextBytes(data);
		chunkDTO.setData(data);

		when(managerServiceMock.uploadClientRoutineBinaryChunk(rbId, chunkDTO)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureResult = client.uploadClientRoutineBinaryChunk(rbId, chunkDTO);
		await().atMost(30, TimeUnit.SECONDS).until(futureResult::isDone);
	}

	@Test
	public void removeClientRoutine() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String rId = UUID.randomUUID().toString();

		when(managerServiceMock.removeClientRoutine(rId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureResult = client.removeClientRoutine(rId);
		await().atMost(30, TimeUnit.SECONDS).until(futureResult::isDone);
	}


	@Test
	public void getServiceEndpoint() throws Exception {
		ServiceEndpointDTO endpoint = client.getServiceEndpoint();
		assertNotNull(endpoint);
		assertEquals(Protocol.DIRECT, endpoint.getProtocol());
	}
}
