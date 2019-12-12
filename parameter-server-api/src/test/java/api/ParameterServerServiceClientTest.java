package api;

import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.parameterserver.api.IParameterServerServiceClient;
import at.enfilo.def.parameterserver.api.client.ParameterServerServiceClientFactory;
import at.enfilo.def.parameterserver.api.thrift.ParameterServerResponseService;
import at.enfilo.def.parameterserver.api.thrift.ParameterServerService;
import at.enfilo.def.transfer.dto.ParameterProtocol;
import at.enfilo.def.transfer.dto.ParameterType;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ParameterServerServiceClientTest {
    private IParameterServerServiceClient client;
    private ParameterServerService.Iface requestServiceMock;
    private ParameterServerResponseService.Iface responseServiceMock;
    private TicketService.Iface ticketServiceMock;

    @Before
    public void setUp() throws Exception {
        requestServiceMock = Mockito.mock(ParameterServerService.Iface.class);
        responseServiceMock = Mockito.mock(ParameterServerResponseService.Iface.class);
        ticketServiceMock = Mockito.mock(TicketService.Iface.class);

        client = new ParameterServerServiceClientFactory().createDirectClient(
                requestServiceMock, responseServiceMock, ticketServiceMock,
                IParameterServerServiceClient.class
        );
    }

    @Test
    public void setParameter() throws Exception {
        String programId = UUID.randomUUID().toString();
        String parameterId = UUID.randomUUID().toString();
        ResourceDTO parameter = new ResourceDTO();
        ParameterProtocol protocol = ParameterProtocol.DEFAULT;
        String ticketId = UUID.randomUUID().toString();

        when(requestServiceMock.setParameter(programId, parameterId, parameter, protocol)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseServiceMock.setParameter(ticketId)).thenReturn(parameterId);

        Future<String> futureResult = client.setParameter(programId, parameterId, parameter, protocol);

        assertEquals(parameterId, futureResult.get());
    }

    @Test
    public void createParameter() throws Exception {
        String programId = UUID.randomUUID().toString();
        String parameterId = UUID.randomUUID().toString();
        ResourceDTO parameter = new ResourceDTO();
        ParameterProtocol protocol = ParameterProtocol.DEFAULT;
        ParameterType type = ParameterType.READ_WRITE;
        String ticketId = UUID.randomUUID().toString();

        // Set up mock methods
        when(requestServiceMock.createParameter(programId, parameterId, parameter, protocol, type)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseServiceMock.createParameter(ticketId)).thenReturn(parameterId);

        Future<String> futureResult = client.createParameter(programId, parameterId, parameter, protocol, type);

        assertEquals(parameterId, futureResult.get());
    }

    @Test
    public void getParameter() throws Exception {
        String programId = UUID.randomUUID().toString();
        String parameterId = UUID.randomUUID().toString();
        ResourceDTO parameter = new ResourceDTO();
        ParameterProtocol protocol = ParameterProtocol.DEFAULT;
        String ticketId = UUID.randomUUID().toString();

        // Set up mock methods
        when(requestServiceMock.getParameter(programId, parameterId, protocol)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseServiceMock.getParameter(ticketId)).thenReturn(parameter);

        Future<ResourceDTO> futureResult = client.getParameter(programId, parameterId, protocol);

        assertEquals(parameter, futureResult.get());
    }

    @Test
    public void addToParameter() throws Exception {
        String programId = UUID.randomUUID().toString();
        String parameterId = UUID.randomUUID().toString();
        ResourceDTO parameter = new ResourceDTO();
        ParameterProtocol protocol = ParameterProtocol.DEFAULT;
        String ticketId = UUID.randomUUID().toString();

        when(requestServiceMock.addToParameter(programId, parameterId, parameter, protocol)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseServiceMock.addToParameter(ticketId)).thenReturn(parameterId);

        Future<String> futureResult = client.addToParameter(programId, parameterId, parameter, protocol);
        assertEquals(parameterId, futureResult.get());
    }

    @Test
    public void deleteAllParameters() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String programId = UUID.randomUUID().toString();

        when(requestServiceMock.deleteAllParameters(programId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseServiceMock.deleteAllParameters(ticketId)).thenReturn(programId);

        Future<String> futureResult = client.deleteAllParameters(programId);
        assertEquals(programId, futureResult.get());
    }

    @Test
    public void deleteParameter() throws Exception {
        String ticketId = UUID.randomUUID().toString();
        String programId = UUID.randomUUID().toString();
        String parameterId = UUID.randomUUID().toString();

        when(requestServiceMock.deleteParameter(programId, parameterId)).thenReturn(ticketId);
        when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
        when(responseServiceMock.deleteParameter(ticketId)).thenReturn(parameterId);

        Future<String> futureResult = client.deleteParameter(programId, parameterId);
        assertEquals(parameterId, futureResult.get());
    }
}
