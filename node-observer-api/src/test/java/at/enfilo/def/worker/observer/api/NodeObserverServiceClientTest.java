package at.enfilo.def.worker.observer.api;

import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.node.observer.api.rest.INodeObserverService;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class NodeObserverServiceClientTest {
	private INodeObserverServiceClient client;
	private INodeObserverService requestService;
	private TicketService.Iface ticketServiceMock;

	@Before
	public void setUp() throws Exception {
		requestService = Mockito.mock(INodeObserverService.class);
		ticketServiceMock = Mockito.mock(TicketService.Iface.class);

		client = new NodeObserverServiceClientFactory().createDirectClient(
			requestService,
			null,
			ticketServiceMock,
			INodeObserverServiceClient.class
		);
	}

	@Test
	public void notifyTasksNewState() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String wId = UUID.randomUUID().toString();
		List<String> tasksDone = new LinkedList<>();
		tasksDone.add(UUID.randomUUID().toString());
		tasksDone.add(UUID.randomUUID().toString());
		Random rnd = new Random();
		ExecutionState state = ExecutionState.values()[rnd.nextInt(ExecutionState.values().length)];

		when(requestService.notifyElementsNewState(wId, tasksDone, state)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(null);

		Future<Void> future = client.notifyElementsNewState(wId, tasksDone, state);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void notifyTasksReceived() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String wId = UUID.randomUUID().toString();
		List<String> tasksDone = new LinkedList<>();
		tasksDone.add(UUID.randomUUID().toString());
		tasksDone.add(UUID.randomUUID().toString());

		when(requestService.notifyTasksReceived(wId, tasksDone)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(null);

		Future<Void> future = client.notifyTasksReceived(wId, tasksDone);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void notifyWorkerInfo() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String nId = UUID.randomUUID().toString();
		NodeInfoDTO info = new NodeInfoDTO();

		when(requestService.notifyNodeInfo(nId, info)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(null);

		Future<Void> future = client.notifyNodeInfo(nId, info);
		await().atMost(30, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void getServiceEndpoint() throws Exception {
		ServiceEndpointDTO endpoint = client.getServiceEndpoint();
		assertNotNull(endpoint);
		assertEquals(Protocol.DIRECT, endpoint.getProtocol());
	}
}
