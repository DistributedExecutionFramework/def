package at.enfilo.def.reducer.api;

import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.reducer.api.rest.IReducerResponseService;
import at.enfilo.def.reducer.api.rest.IReducerService;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
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
import static org.mockito.Mockito.when;

public class ReducerServiceClientTest {

    private IReducerServiceClient client;
    private IReducerService requestService;
    private IReducerResponseService responseService;
    private TicketService.Iface ticketService;

    @Before
    public void setUp() throws Exception {
        requestService = Mockito.mock(IReducerService.class);
        responseService = Mockito.mock(IReducerResponseService.class);
        ticketService = Mockito.mock(TicketService.Iface.class);

        client = new ReducerServiceClientFactory().createDirectClient(
                requestService,
                responseService,
                ticketService,
                IReducerServiceClient.class
        );
    }

    @Test
    public void getQueuedJobs() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String programId = UUID.randomUUID().toString();
        List<String> jobs = new LinkedList<>();
        jobs.add(UUID.randomUUID().toString());
        jobs.add(UUID.randomUUID().toString());

        when(requestService.getQueuedJobs(programId)).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseService.getQueuedJobs(ticketId)).thenReturn(jobs);

        Future<List<String>> future = client.getQueuedJobs(programId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        assertEquals(jobs, future.get());
    }

    @Test
    public void createReduceJob() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        JobDTO job = new JobDTO();

        when(requestService.createReduceJob(job)).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(null);

        Future<Void> future = client.createReduceJob(job);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

    }

    @Test
    public void abortReduceJob() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();

        when(requestService.abortReduceJob(jId)).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(null);

        Future<Void> future = client.abortReduceJob(jId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

    }

    @Test
    public void addResourcesToReduce() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();
        List<ResourceDTO> resources = new LinkedList<>();
        resources.add(new ResourceDTO());

        when(requestService.addResourcesToReduce(jId, resources)).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(null);

        Future<Void> future = client.addResourcesToReduce(jId, resources);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

    }

    @Test
    public void reduceJob() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();

        when(requestService.reduceJob(jId)).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(null);

        Future<Void> future = client.reduceJob(jId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);

    }

    @Test
    public void fetchFinishedJob() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();
        ResourceDTO resource = new ResourceDTO();
        List<ResourceDTO> resources = Arrays.asList(resource);

        when(requestService.fetchResults(jId)).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseService.fetchResults(ticketId)).thenReturn(resources);

        Future<List<ResourceDTO>> future = client.fetchResults(jId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        assertEquals(resources, future.get());
    }

    @Test
    public void getStoreRoutine() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String storeRoutine = UUID.randomUUID().toString();

        when(requestService.getStoreRoutine()).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseService.getStoreRoutine(ticketId)).thenReturn(storeRoutine);

        Future<String> future = client.getStoreRoutine();
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
        assertEquals(storeRoutine, future.get());
    }

    @Test
    public void setStoreRoutine() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String storeRoutineId = UUID.randomUUID().toString();

        when(requestService.setStoreRoutine(storeRoutineId)).thenReturn(ticketId);
        when(ticketService.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.setStoreRoutine(storeRoutineId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

}
