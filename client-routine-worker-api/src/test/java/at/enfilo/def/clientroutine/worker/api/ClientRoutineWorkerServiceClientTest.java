package at.enfilo.def.clientroutine.worker.api;

import at.enfilo.def.clientroutine.worker.api.rest.IClientRoutineWorkerResponseService;
import at.enfilo.def.clientroutine.worker.api.rest.IClientRoutineWorkerService;
import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.transfer.dto.ProgramDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ClientRoutineWorkerServiceClientTest {

    private IClientRoutineWorkerServiceClient client;
    private IClientRoutineWorkerService requestService;
    private IClientRoutineWorkerResponseService responseService;
    private TicketService.Iface ticketService;

    @Before
    public void setUp() throws Exception {
        requestService = Mockito.mock(IClientRoutineWorkerService.class);
        responseService = Mockito.mock(IClientRoutineWorkerResponseService.class);
        ticketService = Mockito.mock(TicketService.Iface.class);

        client = new ClientRoutineWorkerServiceClientFactory().createDirectClient(
                requestService,
                responseService,
                ticketService,
                IClientRoutineWorkerServiceClient.class
        );
    }

    @Test
    public void getQueuedPrograms() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        List<String> programs = new LinkedList<>();
        programs.add(UUID.randomUUID().toString());
        programs.add(UUID.randomUUID().toString());

        when(requestService.getQueuedPrograms(userId)).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseService.getQueuedPrograms(ticketId)).thenReturn(programs);

        Future<List<String>> future = client.getQueuedPrograms(userId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        assertEquals(programs, future.get());
    }

    @Test
    public void queueProgram() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        ProgramDTO program = new ProgramDTO();

        when(requestService.queueProgram(userId, program)).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.queueProgram(userId, program);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void queuePrograms() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        ProgramDTO program = new ProgramDTO();
        List<ProgramDTO> programs = Arrays.asList(program);

        when(requestService.queuePrograms(userId, programs)).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.queuePrograms(userId, programs);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void movePrograms() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String qId = UUID.randomUUID().toString();
        List<String> programs = new LinkedList<>();
        programs.add(UUID.randomUUID().toString());
        programs.add(UUID.randomUUID().toString());
        ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

        when(requestService.movePrograms(qId, programs, endpoint)).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.movePrograms(qId, programs, endpoint);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void moveAllPrograms() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

        when(requestService.moveAllPrograms(endpoint)).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.moveAllPrograms(endpoint);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void fetchFinishedProgram() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String programId = UUID.randomUUID().toString();
        ProgramDTO program = new ProgramDTO();
        program.setId(programId);

        when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(requestService.fetchFinishedProgram(programId)).thenReturn(ticketId);
        when(responseService.fetchFinishedProgram(ticketId)).thenReturn(program);

        Future<ProgramDTO> future = client.fetchFinishedProgram(programId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        assertEquals(program, future.get());
    }

    @Test
    public void abortProgram() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String programId = UUID.randomUUID().toString();

        when(requestService.abortProgram(programId)).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.abortProgram(programId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

}
