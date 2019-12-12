package at.enfilo.def.scheduler.reducer.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.scheduler.reducer.api.IReducerSchedulerServiceClient;
import at.enfilo.def.scheduler.reducer.api.ReducerSchedulerServiceClientFactory;
import at.enfilo.def.scheduler.reducer.api.strategy.IReduceSchedulingStrategy;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class ReducerSchedulerServiceTest {

    private IReducerSchedulerServiceClient client;
    private IServer server;
    private Thread serverThread;

    protected IReduceSchedulingStrategy strategy;

    @Before
    public void setUp() throws Exception {
        TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

        // mocking strategy
        strategy = Mockito.mock(IReduceSchedulingStrategy.class);

        // start server
        server = getServer();
        serverThread = new Thread(server);
        serverThread.start();

        // start client
        await().atMost(10, TimeUnit.SECONDS).until(server::isRunning);
        ReducerSchedulerServiceClientFactory factory = new ReducerSchedulerServiceClientFactory();
        client = factory.createClient(server.getServiceEndpoint());
    }

    protected abstract IServer getServer() throws Exception;

    @Test
    public void addReducer() throws Exception {
        String rId = UUID.randomUUID().toString();
        ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

        Future<Void> future = client.addReducer(rId, endpoint);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).addReducer(rId, endpoint);
    }

    @Test
    public void removeReducer() throws Exception {
        String rId = UUID.randomUUID().toString();

        Future<Void> future = client.removeReducer(rId);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).removeReducer(rId);
    }

    @Test
    public void addReduceJob() throws Exception {
        JobDTO job = new JobDTO();

        Future<Void> future = client.addReduceJob(job);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).addJob(job);
    }

    @Test
    public void removeReduceJob() throws Exception {
        String jId = UUID.randomUUID().toString();

        Future<Void> future = client.removeReduceJob(jId);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).deleteJob(jId);
    }

    @Test
    public void scheduleResourcesToReduce() throws Exception {
        String jId = UUID.randomUUID().toString();
        List<ResourceDTO> resources = new LinkedList<>();
        resources.add(new ResourceDTO());
        resources.add(new ResourceDTO());

        Future<Void> future = client.scheduleResourcesToReduce(jId, resources);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        verify(strategy).scheduleReduce(jId, resources);
    }

    @Test
    public void finalizeReduce() throws Exception {
        String jId = UUID.randomUUID().toString();
        JobDTO job = new JobDTO();

        when(strategy.finalizeReduce(jId)).thenReturn(job);

        Future<JobDTO> future = client.finalizeReduce(jId);

        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        assertEquals(job, future.get());
        verify(strategy).finalizeReduce(jId);
    }

    @After
    public void tearDown() throws Exception {
        client.close();
        server.close();
        serverThread.join();
    }
}
