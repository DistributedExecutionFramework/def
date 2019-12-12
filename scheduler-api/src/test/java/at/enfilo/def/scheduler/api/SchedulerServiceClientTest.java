package at.enfilo.def.scheduler.api;

import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.scheduler.api.rest.ISchedulerResponseService;
import at.enfilo.def.scheduler.api.rest.ISchedulerService;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class SchedulerServiceClientTest {

	private ISchedulerServiceClient client;
	private ISchedulerService schedulerServiceMock;
	private ISchedulerResponseService schedulerResponseServiceMock;
	private TicketService.Iface ticketServiceMock;

	@Before
	public void setUp() throws Exception {
		schedulerServiceMock = Mockito.mock(ISchedulerService.class);
		schedulerResponseServiceMock = Mockito.mock(ISchedulerResponseService.class);
		ticketServiceMock = Mockito.mock(TicketService.Iface.class);

		client = new SchedulerServiceClientFactory().createDirectClient(
				schedulerServiceMock,
				schedulerResponseServiceMock,
				ticketServiceMock,
				ISchedulerServiceClient.class);
	}

	@Test
	public void addJob() throws Exception {
		String jId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();

		when(schedulerServiceMock.addJob(jId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureAddJob = client.addJob(jId);
		assertNull(futureAddJob.get());
	}

	@Test
	public void extendToReduceJob() throws Exception {
		String jId = UUID.randomUUID().toString();
		String reduceRoutineId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();

		when(schedulerServiceMock.extendToReduceJob(jId, reduceRoutineId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureExtendJob = client.extendToReduceJob(jId, reduceRoutineId);
		assertNull(futureExtendJob.get());
	}

	@Test
	public void scheduleTask() throws Exception {
		String jId = UUID.randomUUID().toString();
		TaskDTO task = new TaskDTO();
		task.setId(UUID.randomUUID().toString());
		task.setJobId(jId);
		String ticketId = UUID.randomUUID().toString();

		when(schedulerServiceMock.scheduleTask(jId, task)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureSchedule = client.scheduleTask(jId, task);
		assertNull(futureSchedule.get());
	}

	@Test
	public void scheduleReduce() throws Exception {
		String jId = UUID.randomUUID().toString();
		List<ResourceDTO> resources = new LinkedList<>();
		resources.add(new ResourceDTO());
		String ticketId = UUID.randomUUID().toString();

		when(schedulerServiceMock.scheduleReduce(jId, resources)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureSchedule = client.scheduleResource(jId, resources);
		assertNull(futureSchedule.get());
	}

	@Test
	public void removeJob() throws Exception {
		String jId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();

		when(schedulerServiceMock.removeJob(jId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureAbort = client.removeJob(jId);
		assertNull(futureAbort.get());
	}

	@Test
	public void markJobAsComplete() throws Exception {
		String jId = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();

		when(schedulerServiceMock.markJobAsComplete(jId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureMarkJob = client.markJobAsComplete(jId);
		assertNull(futureMarkJob.get());
	}

	@Test
	public void addWorker() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String workerId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

		when(schedulerServiceMock.addWorker(workerId, endpoint)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureSetWorkers = client.addWorker(workerId, endpoint);
		assertNull(futureSetWorkers.get());
	}

	@Test
	public void removeWorker() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String workerId = UUID.randomUUID().toString();

		when(schedulerServiceMock.removeWorker(workerId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureSetWorkers = client.removeWorker(workerId);
		assertNull(futureSetWorkers.get());
	}

	@Test
	public void addReducer() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String reducerId = UUID.randomUUID().toString();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

		when(schedulerServiceMock.addReducer(reducerId, endpoint)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureSetWorkers = client.addReducer(reducerId, endpoint);
		assertNull(futureSetWorkers.get());
	}

	@Test
	public void removeReducer() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String reducerId = UUID.randomUUID().toString();

		when(schedulerServiceMock.removeReducer(reducerId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

		Future<Void> futureSetWorkers = client.removeReducer(reducerId);
		assertNull(futureSetWorkers.get());
	}

	@Test
	public void finalizeReduce() throws Exception {
		String jId = UUID.randomUUID().toString();
		List<ResourceDTO> resources = new LinkedList<>();
		resources.add(new ResourceDTO());
		String ticketId = UUID.randomUUID().toString();

		when(schedulerServiceMock.finalizeReduce(jId)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(schedulerResponseServiceMock.finalizeReduce(ticketId)).thenReturn(resources);

		Future<List<ResourceDTO>> futureFinalize = client.finalizeReduce(jId);
		assertEquals(resources, futureFinalize.get());
	}



	@Test
	public void getServiceEndpoint() throws Exception {
		ServiceEndpointDTO endpoint = client.getServiceEndpoint();
		assertNotNull(endpoint);
		assertEquals(Protocol.DIRECT, endpoint.getProtocol());
	}
}
