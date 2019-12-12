package at.enfilo.def.cluster.api;

import at.enfilo.def.cluster.api.rest.IClusterResponseService;
import at.enfilo.def.cluster.api.rest.IClusterService;
import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

public class ClusterServiceClientTest {
	private IClusterServiceClient client;
	private IClusterService requestServiceMock;
	private IClusterResponseService responseServiceMock;
	private TicketService.Iface ticketServiceMock;
	private Random rnd;

	@Before
	public void setUp() throws Exception {
		requestServiceMock = Mockito.mock(IClusterService.class);
		responseServiceMock = Mockito.mock(IClusterResponseService.class);
		ticketServiceMock = Mockito.mock(TicketService.Iface.class);

		client = new ClusterServiceClientFactory().createDirectClient(
				requestServiceMock,
				responseServiceMock,
				ticketServiceMock,
				IClusterServiceClient.class
		);

		rnd = new Random();
	}

	@Test
	public void getClusterInfo() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		ClusterInfoDTO cluster = new ClusterInfoDTO();
		when(requestServiceMock.getClusterInfo()).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getClusterInfo(ticketId)).thenReturn(cluster);
		Future<ClusterInfoDTO> futureCluster = client.getClusterInfo();
		assertEquals(cluster, futureCluster.get());
	}

	@Test
	public void getEnvironment() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		List<FeatureDTO> featureDTOS = new ArrayList<>();
		featureDTOS.add(new FeatureDTO());
		when(requestServiceMock.getEnvironment()).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getEnvironment(ticketId)).thenReturn(featureDTOS);
		Future<List<FeatureDTO>> future = client.getEnvironment();
		assertEquals(featureDTOS, future.get());
	}

	@Test
	public void getNodeInfo() throws Exception {
		String wId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		NodeInfoDTO worker = new NodeInfoDTO(
			UUID.randomUUID().toString(),
			UUID.randomUUID().toString(),
			NodeType.WORKER,
			-1,
			-1,
			-1,
			new HashMap<>(),
			"localhost"
		);
		when(requestServiceMock.getNodeInfo(wId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getNodeInfo(ticketId)).thenReturn(worker);
		Future<NodeInfoDTO> futureWorker = client.getNodeInfo(wId);
		assertEquals(worker, futureWorker.get());
	}

	@Test
	public void getNodeEnvironment() throws Exception {
		String wId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		List<FeatureDTO> featureDTOS = new ArrayList<>();
		featureDTOS.add(new FeatureDTO());
		when(requestServiceMock.getNodeEnvironment(wId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getNodeEnvironment(ticketId)).thenReturn(featureDTOS);
		Future<List<FeatureDTO>> future = client.getNodeEnvironment(wId);
		assertEquals(featureDTOS, future.get());
	}

	@Test
	public void destroyCluster() throws Exception {
		client.destroyCluster();
	}

	@Test
	public void getAllWorkers() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		int numberOfWorkers = rnd.nextInt(10) + 1;
		List<String> workers = new LinkedList<>();
		for (int i = 0; i < numberOfWorkers; i++) {
			workers.add(Integer.toString(i));
		}
		NodeType type = NodeType.REDUCER;
		when(requestServiceMock.getAllNodes(type)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getAllNodes(ticketId)).thenReturn(workers);
		Future<List<String>> futureWorkers = client.getAllNodes(type);
		assertEquals(numberOfWorkers, futureWorkers.get().size());
		assertEquals(workers, futureWorkers.get());
	}

	@Test
	public void addWorker() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		NodeType type = NodeType.WORKER;

		when(requestServiceMock.addNode(endpoint, type)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureStatus = client.addNode(endpoint, type);
		assertNull(futureStatus.get());
	}

	@Test
	public void removeWorker() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String nId = UUID.randomUUID().toString();

		when(requestServiceMock.removeNode(nId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureStatus = client.removeNode(nId);
		assertNull(futureStatus.get());
	}


	@Test
	public void getWorkerServiceEndpoint() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();
		String nId = UUID.randomUUID().toString();

		when(requestServiceMock.getNodeServiceEndpoint(nId)).thenReturn(ticketId);
		when(responseServiceMock.getNodeServiceEndpoint(ticketId)).thenReturn(endpoint);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<ServiceEndpointDTO> futureEndpoint = client.getNodeServiceEndpoint(nId);
		assertEquals(endpoint, futureEndpoint.get());
	}


	@Test
	public void getSchedulerEndpoint() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

		when(requestServiceMock.getSchedulerServiceEndpoint(anyObject())).thenReturn(ticketId);
		when(responseServiceMock.getSchedulerServiceEndpoint(ticketId)).thenReturn(endpoint);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<ServiceEndpointDTO> futureEndpoint = client.getSchedulerServiceEndpoint(anyObject());
		assertEquals(endpoint, futureEndpoint.get());
	}


	@Test
	public void setSchedulerEndpoint() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

		when(requestServiceMock.setSchedulerServiceEndpoint(
			NodeType.WORKER,
			endpoint
		)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureStatus = client.setSchedulerServiceEndpoint(
			NodeType.WORKER,
			endpoint
		);
		assertNull(futureStatus.get());
	}

	@Test
	public void getStoreRoutine() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		when(requestServiceMock.getStoreRoutine()).thenReturn(ticketId);
		when(responseServiceMock.getStoreRoutine(ticketId)).thenReturn(routineId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<String> future = client.getStoreRoutine();
		assertEquals(routineId, future.get());
	}


	@Test
	public void setStoreRoutine() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		when(requestServiceMock.setStoreRoutine(routineId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.setStoreRoutine(routineId);
		assertNull(future.get());
	}


	@Test
	public void getDefaultMapRoutine() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		when(requestServiceMock.getDefaultMapRoutine()).thenReturn(ticketId);
		when(responseServiceMock.getDefaultMapRoutine(ticketId)).thenReturn(routineId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<String> future = client.getDefaultMapRoutine();
		assertEquals(routineId, future.get());
	}


	@Test
	public void setDefaultMapRoutine() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		when(requestServiceMock.setDefaultMapRoutine(routineId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.setDefaultMapRoutine(routineId);
		assertNull(future.get());
	}


	@Test
	public void getServiceEndpoint() throws Exception {
		ServiceEndpointDTO endpoint = client.getServiceEndpoint();
		assertNotNull(endpoint);
		assertEquals(Protocol.DIRECT, endpoint.getProtocol());
	}

	@Test
	public void getNodeServiceEndpointConfiguration() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		NodeType nodeType = NodeType.WORKER;
		ServiceEndpointDTO serviceEndpointDTO = new ServiceEndpointDTO();

		when(requestServiceMock.getNodeServiceEndpointConfiguration(nodeType)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getNodeServiceEndpointConfiguration(ticketId)).thenReturn(serviceEndpointDTO);

		Future<ServiceEndpointDTO> futureResult = client.getNodeServiceEndpointConfiguration(nodeType);
		assertEquals(serviceEndpointDTO, futureResult.get());
	}

	@Test
	public void getLibraryEndpointConfiguration() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		ServiceEndpointDTO serviceEndpointDTO = new ServiceEndpointDTO();

		when(requestServiceMock.getLibraryEndpointConfiguration()).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.getLibraryEndpointConfiguration(ticketId)).thenReturn(serviceEndpointDTO);

		Future<ServiceEndpointDTO> futureResult = client.getLibraryEndpointConfiguration();
		assertEquals(serviceEndpointDTO, futureResult.get());
	}

	@Test
	public void findNodesForShutdown() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		NodeType nodeType = NodeType.WORKER;
		int nrOfNodes = 5;
		List<String> list = new LinkedList<>();
		for (int i = 0; i < nrOfNodes; i++) {
			list.add(UUID.randomUUID().toString());
		}

		when(requestServiceMock.findNodesForShutdown(nodeType, nrOfNodes)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseServiceMock.findNodesForShutdown(ticketId)).thenReturn(list);

		Future<List<String>> futureResult = client.findNodesForShutdown(nodeType, nrOfNodes);
		assertEquals(list, futureResult.get());
	}
}
