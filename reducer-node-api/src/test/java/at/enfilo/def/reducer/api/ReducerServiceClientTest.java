package at.enfilo.def.reducer.api;

import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.reducer.api.rest.IReducerResponseService;
import at.enfilo.def.reducer.api.rest.IReducerService;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ReducerServiceClientTest {

	private IReducerServiceClient client;
	private IReducerService requestService;
	private IReducerResponseService responseService;
	private TicketService.Iface ticketService;

	@Before
	public void setUp() throws Exception {
		requestService = Mockito.mock(IReducerService.class);
		responseService = Mockito.mock(IReducerResponseService.class);
		ticketService = Mockito.mock(TicketService.Iface.class);

		client = new ReducerServiceClientFactory().createDirectClient(
				requestService,
				responseService,
				ticketService,
				IReducerServiceClient.class
		);
	}

	@Test
	public void createReduceJob() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		when(requestService.createReduceJob(jId, routineId)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(null);

		Future<Void> future = client.createReduceJob(jId, routineId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void deleteReduceJob() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();

		when(requestService.deleteReduceJob(jId)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(null);

		Future<Void> future = client.deleteReduceJob(jId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void add() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		List<ResourceDTO> resources = new LinkedList<>();
		resources.add(new ResourceDTO());
		resources.add(new ResourceDTO());

		when(requestService.add(jId, resources)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(null);

		Future<Void> future = client.add(jId, resources);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void reduce() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();

		when(requestService.reduce(jId)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(null);

		Future<Void> future = client.reduce(jId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void fetchResults() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String jId = UUID.randomUUID().toString();
		List<ResourceDTO> results = new LinkedList<>();
		results.add(new ResourceDTO());

		when(requestService.fetchResult(jId)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.fetchResult(ticketId)).thenReturn(results);

		Future<List<ResourceDTO>> future = client.fetchResult(jId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(results, future.get());
	}
}
