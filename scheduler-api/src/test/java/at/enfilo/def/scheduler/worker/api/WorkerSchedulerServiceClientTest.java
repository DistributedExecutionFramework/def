package at.enfilo.def.scheduler.worker.api;

import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.scheduler.worker.api.rest.IWorkerSchedulerResponseService;
import at.enfilo.def.scheduler.worker.api.rest.IWorkerSchedulerService;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class WorkerSchedulerServiceClientTest {

    private IWorkerSchedulerServiceClient client;
    private IWorkerSchedulerService schedulerServiceMock;
    private IWorkerSchedulerResponseService schedulerResponseServiceMock;
    private TicketService.Iface ticketServiceMock;

    @Before
    public void setUp() throws Exception {
        schedulerServiceMock = Mockito.mock(IWorkerSchedulerService.class);
        schedulerResponseServiceMock = Mockito.mock(IWorkerSchedulerResponseService.class);
        ticketServiceMock = Mockito.mock(TicketService.Iface.class);

        client = new WorkerSchedulerServiceClientFactory().createDirectClient(
                schedulerServiceMock,
                schedulerResponseServiceMock,
                ticketServiceMock,
                IWorkerSchedulerServiceClient.class
        );
    }

    @Test
    public void addWorker() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String wId = UUID.randomUUID().toString();
        ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

        when(schedulerServiceMock.addWorker(wId, endpoint)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.addWorker(wId, endpoint);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void removeWorker() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String wId = UUID.randomUUID().toString();

        when(schedulerServiceMock.removeWorker(wId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.removeWorker(wId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void addJob() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();

        when(schedulerServiceMock.addJob(jId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.addJob(jId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void removeJob() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();

        when(schedulerServiceMock.removeJob(jId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.removeJob(jId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void scheduleTask() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();
        TaskDTO task = new TaskDTO();

        when(schedulerServiceMock.scheduleTask(jId, task)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.scheduleTask(jId, task);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void markJobAsComplete() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();

        when(schedulerServiceMock.markJobAsComplete(jId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.markJobAsComplete(jId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void getServiceEndpoint() throws Exception {
        ServiceEndpointDTO endpoint = client.getServiceEndpoint();
        assertNotNull(endpoint);
        assertEquals(Protocol.DIRECT, endpoint.getProtocol());
    }
}
