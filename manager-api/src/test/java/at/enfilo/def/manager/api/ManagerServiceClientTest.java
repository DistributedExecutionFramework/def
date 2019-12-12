package at.enfilo.def.manager.api;

import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.manager.api.rest.IManagerResponseService;
import at.enfilo.def.manager.api.rest.IManagerService;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;

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
		assertNull(futureDeleteCluster.get());
	}

	@Test
	public void addCluster() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpointDTO = new ServiceEndpointDTO();

		when(managerServiceMock.addCluster(endpointDTO)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureAddCluster = client.addCluster(endpointDTO);
		assertNull(futureAddCluster.get());
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
		assertNull(futureResult.get());
	}

	@Test
	public void getServiceEndpoint() throws Exception {
		ServiceEndpointDTO endpoint = client.getServiceEndpoint();
		assertNotNull(endpoint);
		assertEquals(Protocol.DIRECT, endpoint.getProtocol());
	}
}
