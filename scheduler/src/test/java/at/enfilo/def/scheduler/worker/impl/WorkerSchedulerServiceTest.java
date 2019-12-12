package at.enfilo.def.scheduler.worker.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.scheduler.worker.api.IWorkerSchedulerServiceClient;
import at.enfilo.def.scheduler.worker.api.WorkerSchedulerServiceClientFactory;
import at.enfilo.def.scheduler.worker.api.strategy.ITaskSchedulingStrategy;
import at.enfilo.def.transfer.dto.TaskDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

public abstract class WorkerSchedulerServiceTest {

    private IWorkerSchedulerServiceClient client;
    private IServer server;
    private Thread serverThread;

    protected ITaskSchedulingStrategy strategy;

    @Before
    public void setUp() throws Exception {
        TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

        // mocking scheduling strategy
        strategy = Mockito.mock(ITaskSchedulingStrategy.class);

        // start server
        server = getServer();
        serverThread = new Thread(server);
        serverThread.start();

        // start client
        await().atMost(10, TimeUnit.SECONDS).until(server::isRunning);
        WorkerSchedulerServiceClientFactory factory = new WorkerSchedulerServiceClientFactory();
        client = factory.createClient(server.getServiceEndpoint());
    }

    protected abstract IServer getServer() throws Exception;

    @Test
    public void addWorker() throws Exception {
        String wId = UUID.randomUUID().toString();
        ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

        Future<Void> future = client.addWorker(wId, endpoint);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).addWorker(wId, endpoint);
    }

    @Test
    public void removeWorker() throws Exception {
        String wId = UUID.randomUUID().toString();

        Future<Void> future = client.removeWorker(wId);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).removeWorker(wId);
    }

    @Test
    public void addJob() throws Exception {
        String jId = UUID.randomUUID().toString();

        Future<Void> future = client.addJob(jId);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).addJob(jId);
    }

    @Test
    public void removeJob() throws Exception {
        String jId = UUID.randomUUID().toString();

        Future<Void> future = client.removeJob(jId);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).removeJob(jId);
    }

    @Test
    public void scheduleTask() throws Exception {
        String jId = UUID.randomUUID().toString();
        TaskDTO task = new TaskDTO();

        Future<Void> future = client.scheduleTask(jId, task);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).schedule(jId, Collections.singletonList(task));
    }

    @Test
    public void markJobAsComplete() throws Exception {
        String jId = UUID.randomUUID().toString();

        Future<Void> future = client.markJobAsComplete(jId);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).markJobAsComplete(jId);
    }

    @After
    public void tearDown() throws Exception {
        client.close();
        server.close();
        serverThread.join();
    }
}
