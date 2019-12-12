package at.enfilo.def.scheduler.clientroutineworker.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.scheduler.clientroutineworker.api.ClientRoutineWorkerSchedulerServiceClientFactory;
import at.enfilo.def.scheduler.clientroutineworker.api.IClientRoutineWorkerSchedulerServiceClient;
import at.enfilo.def.scheduler.clientroutineworker.api.strategy.IProgramSchedulingStrategy;
import at.enfilo.def.transfer.dto.ProgramDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

public abstract class ClientRoutineWorkerSchedulerServiceTest {

    private IClientRoutineWorkerSchedulerServiceClient client;
    private IServer server;
    private Thread serverThread;

    protected IProgramSchedulingStrategy strategy;

    @Before
    public void setUp() throws Exception {
        TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

        // mocking scheduling strategy
        strategy = Mockito.mock(IProgramSchedulingStrategy.class);

        // start server
        server = getServer();
        serverThread = new Thread(server);
        serverThread.start();

        // start client
        await().atMost(10, TimeUnit.SECONDS).until(server::isRunning);
        ClientRoutineWorkerSchedulerServiceClientFactory factory = new ClientRoutineWorkerSchedulerServiceClientFactory();
        client = factory.createClient(server.getServiceEndpoint());
    }

    protected abstract IServer getServer() throws Exception;

    @Test
    public void addClientRoutineWorker() throws Exception {
        String wId = UUID.randomUUID().toString();
        ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

        Future<Void> future = client.addClientRoutineWorker(wId, endpoint);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).addClientRoutineWorker(wId, endpoint);
    }

    @Test
    public void removeClientRoutineWorker() throws Exception {
        String wId = UUID.randomUUID().toString();

        Future<Void> future = client.removeClientRoutineWorker(wId);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).removeClientRoutineWorker(wId);
    }

    @Test
    public void addUser() throws Exception {
        String uId = UUID.randomUUID().toString();

        Future<Void> future = client.addUser(uId);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).addUser(uId);
    }

    @Test
    public void removeUser() throws Exception {
        String uId = UUID.randomUUID().toString();

        Future<Void> future = client.removeUser(uId);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).removeUser(uId);
    }

    @Test
    public void abortProgram() throws Exception {
        String wId = UUID.randomUUID().toString();
        String pId = UUID.randomUUID().toString();

        Future<Void> future = client.abortProgram(wId, pId);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).abortProgram(wId, pId);
    }

    @Test
    public void scheduleProgram() throws Exception {
        String uId = UUID.randomUUID().toString();
        ProgramDTO program = new ProgramDTO();

        Future<Void> future = client.scheduleProgram(uId, program);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).scheduleProgram(uId, program);
    }

    @After
    public void tearDown() throws Exception {
        client.close();
        server.close();
        serverThread.join();
    }
}
