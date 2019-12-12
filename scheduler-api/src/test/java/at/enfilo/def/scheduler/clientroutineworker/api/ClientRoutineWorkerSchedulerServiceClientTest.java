package at.enfilo.def.scheduler.clientroutineworker.api;

import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.scheduler.clientroutineworker.api.rest.IClientRoutineWorkerSchedulerResponseService;
import at.enfilo.def.scheduler.clientroutineworker.api.rest.IClientRoutineWorkerSchedulerService;
import at.enfilo.def.transfer.dto.ProgramDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

public class ClientRoutineWorkerSchedulerServiceClientTest {

    private IClientRoutineWorkerSchedulerServiceClient client;
    private IClientRoutineWorkerSchedulerService schedulerServiceMock;
    private IClientRoutineWorkerSchedulerResponseService schedulerResponseServiceMock;
    private TicketService.Iface ticketServiceMock;

    @Before
    public void setUp() throws Exception {
        schedulerServiceMock = Mockito.mock(IClientRoutineWorkerSchedulerService.class);
        schedulerResponseServiceMock = Mockito.mock(IClientRoutineWorkerSchedulerResponseService.class);
        ticketServiceMock = Mockito.mock(TicketService.Iface.class);

        client = new ClientRoutineWorkerSchedulerServiceClientFactory().createDirectClient(
                schedulerServiceMock,
                schedulerResponseServiceMock,
                ticketServiceMock,
                IClientRoutineWorkerSchedulerServiceClient.class
        );
    }

    @Test
    public void addClientRoutineWorker() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String wId = UUID.randomUUID().toString();
        ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

        when(schedulerServiceMock.addClientRoutineWorker(wId, endpoint)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.addClientRoutineWorker(wId, endpoint);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void removeClientRoutineWorker() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String wId = UUID.randomUUID().toString();

        when(schedulerServiceMock.removeClientRoutineWorker(wId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.removeClientRoutineWorker(wId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void addUser() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String uId = UUID.randomUUID().toString();

        when(schedulerServiceMock.addUser(uId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.addUser(uId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void removeUser() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String uId = UUID.randomUUID().toString();

        when(schedulerServiceMock.removeUser(uId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.removeUser(uId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void abortProgram() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String wId = UUID.randomUUID().toString();
        String pId = UUID.randomUUID().toString();

        when(schedulerServiceMock.abortProgram(wId, pId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.abortProgram(wId, pId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void scheduleProgram() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String uId = UUID.randomUUID().toString();
        ProgramDTO program = new ProgramDTO();

        when(schedulerServiceMock.scheduleProgram(uId, program)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.scheduleProgram(uId, program);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }
}
