package at.enfilo.def.cloud.communication.api;

import at.enfilo.def.cloud.communication.api.rest.ICloudCommunicationResponseService;
import at.enfilo.def.cloud.communication.api.rest.ICloudCommunicationService;
import at.enfilo.def.cloud.communication.dto.AWSSpecificationDTO;
import at.enfilo.def.cloud.communication.dto.InstanceTypeDTO;
import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class CloudCommunicationServiceClientTest {

    private ICloudCommunicationServiceClient client;
    private ICloudCommunicationService requestServiceMock;
    private ICloudCommunicationResponseService responseServiceMock;
    private TicketService.Iface ticketServiceMock;

    @Before
    public void setUp() throws Exception {
        requestServiceMock = Mockito.mock(ICloudCommunicationService.class);
        responseServiceMock = Mockito.mock(ICloudCommunicationResponseService.class);
        ticketServiceMock = Mockito.mock(TicketService.Iface.class);

        client = new CloudCommunicationServiceClientFactory().createDirectClient(
                requestServiceMock,
                responseServiceMock,
                ticketServiceMock,
                ICloudCommunicationServiceClient.class
        );
    }

    @Test
    public void createAWSClusterTest() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        AWSSpecificationDTO specificationDTO = new AWSSpecificationDTO();
        String cId = UUID.randomUUID().toString();

        when(requestServiceMock.createAWSCluster(specificationDTO)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseServiceMock.createAWSCluster(ticketId)).thenReturn(cId);

        Future<String> futureResult = client.createAWSCluster(specificationDTO);
        assertEquals(cId, futureResult.get());
    }

    @Test
    public void bootClusterInstanceTest() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String cloudClusterId = UUID.randomUUID().toString();
        String clusterInstanceId = UUID.randomUUID().toString();

        when(requestServiceMock.bootClusterInstance(cloudClusterId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseServiceMock.bootClusterInstance(ticketId)).thenReturn(clusterInstanceId);

        Future<String> futureResult = client.bootClusterInstance(cloudClusterId);
        assertEquals(clusterInstanceId, futureResult.get());
    }

    @Test
    public void bootNodesTest() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String cloudClusterId = UUID.randomUUID().toString();
        InstanceTypeDTO instanceTypeDTO = InstanceTypeDTO.REDUCER;
        int nrOfNodes = 4;
        List<String> list = new LinkedList<>();
        for (int i = 0; i < nrOfNodes; i++) {
            list.add(UUID.randomUUID().toString());
        }

        when(requestServiceMock.bootNodes(cloudClusterId, instanceTypeDTO, nrOfNodes)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseServiceMock.bootNodes(ticketId)).thenReturn(list);

        Future<List<String>> futureResult = client.bootNodes(cloudClusterId, instanceTypeDTO, nrOfNodes);
        assertEquals(list, futureResult.get());
    }

    @Test
    public void terminateNodesTest() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String cloudClusterId = UUID.randomUUID().toString();
        List<String> list = new LinkedList<>();

        when(requestServiceMock.terminateNodes(cloudClusterId, list)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> futureResult = client.terminateNodes(cloudClusterId, list);
        assertEquals(null, futureResult.get());
    }

    @Test
    public void getPublicIPAddressOfCloudInstanceTest() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String cloudClusterId = UUID.randomUUID().toString();
        String cloudInstanceId = UUID.randomUUID().toString();
        String publicIPAddress = UUID.randomUUID().toString();

        when(requestServiceMock.getPublicIPAddressOfCloudInstance(cloudClusterId, cloudInstanceId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseServiceMock.getPublicIPAddressOfCloudInstance(ticketId)).thenReturn(publicIPAddress);

        Future<String> futureResult = client.getPublicIPAddressOfCloudInstance(cloudClusterId, cloudInstanceId);
        assertEquals(publicIPAddress, futureResult.get());
    }

    @Test
    public void getPrivateIPAddressOfCloudInstanceTest() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String cloudClusterId = UUID.randomUUID().toString();
        String cloudInstanceId = UUID.randomUUID().toString();
        String privateIPAddress = UUID.randomUUID().toString();

        when(requestServiceMock.getPrivateIPAddressOfCloudInstance(cloudClusterId, cloudInstanceId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseServiceMock.getPrivateIPAddressOfCloudInstance(ticketId)).thenReturn(privateIPAddress);

        Future<String> futureResult = client.getPrivateIPAddressOfCloudInstance(cloudClusterId, cloudInstanceId);
        assertEquals(privateIPAddress, futureResult.get());

    }

    @Test
    public void shutdownCloudClusterTest() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String cloudClusterId = UUID.randomUUID().toString();

        when(requestServiceMock.shutdownCloudCluster(cloudClusterId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> futureResult = client.shutdownCloudCluster(cloudClusterId);
        assertEquals(null, futureResult.get());
    }

    @Test
    public void mapDEFIdToCloudInstanceIdTest() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String cloudClusterId = UUID.randomUUID().toString();
        String defId = UUID.randomUUID().toString();
        String cloudInstanceId = UUID.randomUUID().toString();

        when(requestServiceMock.mapDEFIdToCloudInstanceId(cloudClusterId, defId, cloudInstanceId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);

        Future<Void> futureResult = client.mapDEFIdToCloudInstanceId(cloudClusterId, defId, cloudInstanceId);
        assertEquals(null, futureResult.get());
    }
}
