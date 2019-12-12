package at.enfilo.def.node.api;

import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.node.api.rest.INodeResponseService;
import at.enfilo.def.node.api.rest.INodeService;
import at.enfilo.def.transfer.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NodeServiceClientTest {
	private INodeServiceClient client;
	private INodeService requestService;
	private INodeResponseService responseService;
	private TicketService.Iface ticketService;

	@Before
	public void setUp() throws Exception {
		requestService = Mockito.mock(INodeService.class);
		responseService = Mockito.mock(INodeResponseService.class);
		ticketService = Mockito.mock(TicketService.Iface.class);

		client = new NodeServiceClientFactory().createDirectClient(
				requestService,
				responseService,
				ticketService,
				INodeServiceClient.class
		);
	}

	@Test
	public void takeControl() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String clusterId = UUID.randomUUID().toString();

		when(requestService.takeControl(clusterId)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.takeControl(clusterId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
	}

	@Test
	public void registerObserver() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

		when(requestService.registerObserver(endpoint, false, 0, PeriodUnit.SECONDS)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.registerObserver(endpoint, false, 0, PeriodUnit.SECONDS);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
	}

	@Test
	public void deregisterObserver() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

		when(requestService.deregisterObserver(endpoint)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.deregisterObserver(endpoint);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
	}

	@Test
	public void shutdown() throws Exception {
		client.shutdown();
		verify(requestService).shutdown();
	}

	@Test
	public void getInfo() throws Exception {
		String ticketId = UUID.randomUUID().toString();

		Set<String> runningTasks = new HashSet<>();
		runningTasks.add(UUID.randomUUID().toString());
		runningTasks.add(UUID.randomUUID().toString());

		NodeInfoDTO info = new NodeInfoDTO(
			UUID.randomUUID().toString(),
			UUID.randomUUID().toString(),
			NodeType.WORKER,
			0,
			0,
			0,
			new HashMap<>(),
			"localhost"
		);

		when(requestService.getInfo()).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.getInfo(ticketId)).thenReturn(info);

		Future<NodeInfoDTO> future = client.getInfo();
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(info, future.get());
	}

	@Test
	public void getEnvironment() throws Exception {
		String ticketId = UUID.randomUUID().toString();

		NodeEnvironmentDTO env = new NodeEnvironmentDTO(UUID.randomUUID().toString(), new ArrayList<>());

		when(requestService.getEnvironment()).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.getEnvironment(ticketId)).thenReturn(env);

		Future<NodeEnvironmentDTO> future = client.getEnvironment();
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(env, future.get());
	}

	@Test
	public void getFeatures() throws Exception {
		String ticketId = UUID.randomUUID().toString();

		List<FeatureDTO> env = new ArrayList<>();
		env.add(new FeatureDTO());

		when(requestService.getFeatures()).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.getFeatures(ticketId)).thenReturn(env);

		Future<List<FeatureDTO>> future = client.getFeatures();
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(env, future.get());
	}

	@Test
	public void getServiceEndpoint() throws Exception {
		ServiceEndpointDTO endpoint = client.getServiceEndpoint();
		assertNotNull(endpoint);
		assertEquals(Protocol.DIRECT, endpoint.getProtocol());
	}

	@Test
	public void addSharedResource() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		ResourceDTO sharedResource = new ResourceDTO();
		sharedResource.setId(UUID.randomUUID().toString());
		sharedResource.setDataTypeId(UUID.randomUUID().toString());

		when(requestService.addSharedResource(sharedResource)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.addSharedResource(sharedResource);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
	}

	@Test
	public void removeSharedResource() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		List<String> rIds = new LinkedList<>();
		rIds.add(UUID.randomUUID().toString());
		rIds.add(UUID.randomUUID().toString());

		when(requestService.removeSharedResources(rIds)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.removeSharedResources(rIds);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertNull(future.get());
	}
}


