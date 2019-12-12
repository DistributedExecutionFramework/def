package at.enfilo.def.scheduler.reducer.api;

import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.scheduler.reducer.api.rest.IReducerSchedulerResponseService;
import at.enfilo.def.scheduler.reducer.api.rest.IReducerSchedulerService;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class ReducerSchedulerServiceClientTest {

    private IReducerSchedulerServiceClient client;
    private IReducerSchedulerService schedulerServiceMock;
    private IReducerSchedulerResponseService schedulerResponseServiceMock;
    private TicketService.Iface ticketServiceMock;

    @Before
    public void setUp() throws Exception {
        schedulerServiceMock = Mockito.mock(IReducerSchedulerService.class);
        schedulerResponseServiceMock = Mockito.mock(IReducerSchedulerResponseService.class);
        ticketServiceMock = Mockito.mock(TicketService.Iface.class);

        client = new ReducerSchedulerServiceClientFactory().createDirectClient(
                schedulerServiceMock,
                schedulerResponseServiceMock,
                ticketServiceMock,
                IReducerSchedulerServiceClient.class
        );
    }

    @Test
    public void addReducer() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String rId = UUID.randomUUID().toString();
        ServiceEndpointDTO endpoint = new ServiceEndpointDTO();

        when(schedulerServiceMock.addReducer(rId, endpoint)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.addReducer(rId, endpoint);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void removeReducer() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String rId = UUID.randomUUID().toString();

        when(schedulerServiceMock.removeReducer(rId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.removeReducer(rId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void addReduceJob() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        JobDTO job = new JobDTO();

        when(schedulerServiceMock.addReduceJob(job)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.addReduceJob(job);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void removeReduceJob() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();

        when(schedulerServiceMock.removeReduceJob(jId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.removeReduceJob(jId);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void scheduleResourcesToReduce() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();
        ResourceDTO resource = new ResourceDTO();
        List<ResourceDTO> resources = Arrays.asList(resource);

        when(schedulerServiceMock.scheduleResourcesToReduce(jId, resources)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> future = client.scheduleResourcesToReduce(jId, resources);
        await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
    }

    @Test
    public void finalizeReduce() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();
        JobDTO job = new JobDTO();

        when(schedulerServiceMock.finalizeReduce(jId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(schedulerResponseServiceMock.finalizeReduce(ticketId)).thenReturn(job);

        Future<JobDTO> future = client.finalizeReduce(jId);
        assertEquals(job, future.get());
    }

    @Test
    public void getServiceEndpoint() throws Exception {
        ServiceEndpointDTO endpoint = client.getServiceEndpoint();
        assertNotNull(endpoint);
        assertEquals(Protocol.DIRECT, endpoint.getProtocol());
    }
}
