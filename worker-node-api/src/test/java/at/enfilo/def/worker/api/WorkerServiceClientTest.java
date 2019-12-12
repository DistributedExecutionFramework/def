package at.enfilo.def.worker.api;

import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.rest.IWorkerResponseService;
import at.enfilo.def.worker.api.rest.IWorkerService;
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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class WorkerServiceClientTest {
	private IWorkerServiceClient client;
	private IWorkerService requestService;
	private IWorkerResponseService responseService;
	private TicketService.Iface ticketService;

	@Before
	public void setUp() throws Exception {
		requestService = Mockito.mock(IWorkerService.class);
		responseService = Mockito.mock(IWorkerResponseService.class);
		ticketService = Mockito.mock(TicketService.Iface.class);

		client = new WorkerServiceClientFactory().createDirectClient(
				requestService,
				responseService,
				ticketService,
				IWorkerServiceClient.class
		);
	}

	@Test
	public void queueTasks() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String queueId = UUID.randomUUID().toString();
		List<TaskDTO> tasks = new LinkedList<>();
		tasks.add(new TaskDTO());
		tasks.add(new TaskDTO());

		when(requestService.queueTasks(queueId, tasks)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.queueTasks(queueId, tasks);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void getQueuedTasks() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String queueId = UUID.randomUUID().toString();
		List<String> tasks = new LinkedList<>();
		tasks.add(UUID.randomUUID().toString());
		tasks.add(UUID.randomUUID().toString());

		when(requestService.getQueuedTasks(queueId)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.getQueuedTasks(ticketId)).thenReturn(tasks);

		Future<List<String>> future = client.getQueuedTasks(queueId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(tasks, future.get());
	}

	@Test
	public void moveTask() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String queueId = UUID.randomUUID().toString();
		List<String> tasks = new LinkedList<>();
		tasks.add(UUID.randomUUID().toString());
		tasks.add(UUID.randomUUID().toString());
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

		when(requestService.moveTasks(queueId, tasks, endpoint)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.moveTasks(queueId, tasks, endpoint);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void fetchFinishedTask() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String taskId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();

		when(requestService.fetchFinishedTask(taskId)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.fetchFinishedTask(ticketId)).thenReturn(task);

		Future<TaskDTO> future = client.fetchFinishedTask(taskId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(task, future.get());
	}

	@Test
	public void moveAllTasks() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

		when(requestService.moveAllTasks(endpoint)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.moveAllTasks(endpoint);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void getStoreRoutine() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		when(requestService.getStoreRoutine()).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(responseService.getStoreRoutine(ticketId)).thenReturn(routineId);

		Future<String> future = client.getStoreRoutine();
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(routineId, future.get());
	}

	@Test
	public void setStoreRoutine() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String routineId = UUID.randomUUID().toString();

		when(requestService.setStoreRoutine(routineId)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.setStoreRoutine(routineId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
	}

	@Test
	public void getServiceEndpoint() throws Exception {
		ServiceEndpointDTO endpoint = client.getServiceEndpoint();
		assertNotNull(endpoint);
		assertEquals(Protocol.DIRECT, endpoint.getProtocol());
	}

	@Test
	public void abortTask() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String taskId = UUID.randomUUID().toString();

		when(requestService.abortTask(taskId)).thenReturn(ticketId);
		when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> future = client.abortTask(taskId);
		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
	}
}


