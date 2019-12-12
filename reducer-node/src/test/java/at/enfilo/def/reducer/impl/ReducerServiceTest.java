package at.enfilo.def.reducer.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.reducer.api.IReducerServiceClient;
import at.enfilo.def.reducer.api.ReducerServiceClientFactory;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class ReducerServiceTest {

    private IServer server;
    private Thread serverThread;
    private IReducerServiceClient client;
    private ReducerServiceController controller;

    @Before
    public void setUp() throws Exception {
        TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

        // Mocking internal services
        controller = Mockito.mock(ReducerServiceController.class);

        // Start server
        server = getServer(controller);
        serverThread = new Thread(server);
        serverThread.start();

        await().atMost(30, TimeUnit.SECONDS).until(server::isRunning);
        ReducerServiceClientFactory factory = new ReducerServiceClientFactory();
        client = factory.createClient(server.getServiceEndpoint());
    }

    protected abstract IServer getServer(ReducerServiceController controller) throws Exception;

    @Test
    public void getQueuedJobs() throws Exception {
        String qId = UUID.randomUUID().toString();
        List<String> jobs = new LinkedList<>();
        jobs.add(UUID.randomUUID().toString());
        jobs.add(UUID.randomUUID().toString());

        when(controller.getQueuedElements(qId)).thenReturn(jobs);

        Future<List<String>> future = client.getQueuedJobs(qId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

        List<String> requestedJobs = future.get();
        assertEquals(jobs, requestedJobs);
    }

    @Test
    public void createReduceJob() throws Exception {
        String pId = UUID.randomUUID().toString();
        JobDTO job = new JobDTO();

        Future<Void> future = client.createReduceJob(job);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

        verify(controller).createReduceJob(job);
    }

    @Test
    public void abortReduceJob() throws Exception {
        String jId = UUID.randomUUID().toString();

        Future<Void> future = client.abortReduceJob(jId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

        verify(controller).abortReduceJob(jId);
    }

    @Test
    public void addResourcesToReduce() throws Exception {
        String jId = UUID.randomUUID().toString();
        ResourceDTO resource1 = new ResourceDTO();
        ResourceDTO resource2 = new ResourceDTO();
        List<ResourceDTO> resources = Arrays.asList(resource1, resource2);

        Future<Void> future = client.addResourcesToReduce(jId, resources);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

        verify(controller).addResourcesToReduce(jId, resources);
    }

    @Test
    public void reduceJob() throws Exception {
        String jId = UUID.randomUUID().toString();

        Future<Void> future = client.reduceJob(jId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

        verify(controller).reduceJob(jId);
    }

    @Test
    public void fetchFinishedJob() throws Exception {
        String jId = UUID.randomUUID().toString();
        ResourceDTO resource = new ResourceDTO();
        List<ResourceDTO> resources = Arrays.asList(resource);

        when(controller.fetchResults(jId)).thenReturn(resources);

        Future<List<ResourceDTO>> future = client.fetchResults(jId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

        List<ResourceDTO> requestedResources = future.get();
        assertEquals(resources, requestedResources);
    }


    @After
    public void tearDown() throws Exception {
        client.close();
        server.close();
        serverThread.join();
    }
}
