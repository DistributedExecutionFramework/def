package at.enfilo.def.clientroutine.worker.impl;

import at.enfilo.def.clientroutine.worker.api.ClientRoutineWorkerServiceClientFactory;
import at.enfilo.def.clientroutine.worker.api.IClientRoutineWorkerServiceClient;
import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.transfer.dto.ProgramDTO;
import org.junit.After;
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
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class ClientRoutineWorkerServiceTest {
    private IServer server;
    private Thread serverThread;
    private IClientRoutineWorkerServiceClient client;
    private ClientRoutineWorkerServiceController controller;

    @Before
    public void setUp() throws Exception {
        TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

        // Mocking internal services
        controller = Mockito.mock(ClientRoutineWorkerServiceController.class);

        // Start server
        server = getServer(controller);
        serverThread = new Thread(server);
        serverThread.start();

        await().atMost(30, TimeUnit.SECONDS).until(server::isRunning);
        ClientRoutineWorkerServiceClientFactory factory = new ClientRoutineWorkerServiceClientFactory();
        client = factory.createClient(server.getServiceEndpoint());
    }

    protected abstract IServer getServer(ClientRoutineWorkerServiceController controller) throws Exception;


    @Test
    public void getQueuedPrograms() throws Exception {
        String qId = UUID.randomUUID().toString();
        List<String> programs = new LinkedList<>();
        programs.add(UUID.randomUUID().toString());
        programs.add(UUID.randomUUID().toString());

        when(controller.getQueuedElements(qId)).thenReturn(programs);

        Future<List<String>> future = client.getQueuedPrograms(qId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

        List<String> requestedPrograms = future.get();

        assertEquals(programs, requestedPrograms);
    }

    @Test
    public void queuePrograms() throws Exception {
        String qId = UUID.randomUUID().toString();
        List<ProgramDTO> programs = new LinkedList<>();
        programs.add(new ProgramDTO());
        programs.add(new ProgramDTO());

        Future<Void> future = client.queuePrograms(qId, programs);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

        verify(controller).queueElements(qId, programs);
    }

    @Test
    public void queueProgram() throws Exception {
        String qId = UUID.randomUUID().toString();
        ProgramDTO program = new ProgramDTO();

        Future<Void> future = client.queueProgram(qId, program);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

        verify(controller).queueElements(anyString(), anyList());
    }

    @Test
    public void movePrograms() throws Exception {
        String qId = UUID.randomUUID().toString();
        List<String> programs = new LinkedList<>();
        programs.add(UUID.randomUUID().toString());
        programs.add(UUID.randomUUID().toString());
        ServiceEndpointDTO worker = new ServiceEndpointDTO();

        Future<Void> future = client.movePrograms(qId, programs, worker);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

        verify(controller).moveElements(qId, programs, worker);
    }

    @Test
    public void moveAllPrograms() throws Exception {
        ServiceEndpointDTO worker = new ServiceEndpointDTO();

        Future<Void> future = client.moveAllPrograms(worker);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

        verify(controller).moveAllElements(worker);
    }

    @Test
    public void fetchFinishedProgram() throws Exception {
        String pId = UUID.randomUUID().toString();
        ProgramDTO program = new ProgramDTO();

        when(controller.fetchFinishedElement(pId)).thenReturn(program);

        Future<ProgramDTO> future = client.fetchFinishedProgram(pId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

        ProgramDTO requestedProgram = future.get();

        assertEquals(program, requestedProgram);
    }

    @Test
    public void abortProgram() throws Exception {
        String pId = UUID.randomUUID().toString();

        Future<Void> future = client.abortProgram(pId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

    }

    @After
    public void tearDown() throws Exception {
        client.close();
        server.close();
        serverThread.join();
    }
}
