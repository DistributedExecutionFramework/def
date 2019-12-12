package at.enfilo.def.manager.api;

import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.manager.api.rest.IAuthResponseService;
import at.enfilo.def.manager.api.rest.IAuthService;
import at.enfilo.def.transfer.dto.AuthDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class AuthServiceClientTest {
	private IAuthServiceClient client;
	private IAuthService authServiceMock;
	private IAuthResponseService authResponseServiceMock;
	private TicketService.Iface ticketServiceMock;

	@Before
	public void setUp() throws Exception {
		authServiceMock = Mockito.mock(IAuthService.class);
		authResponseServiceMock = Mockito.mock(IAuthResponseService.class);
		ticketServiceMock = Mockito.mock(TicketService.Iface.class);

		client = new AuthServiceClientFactory().createDirectClient(
				authServiceMock,
				authResponseServiceMock,
				ticketServiceMock,
				IAuthServiceClient.class
		);
	}


	@Test
	public void getToken() throws Exception {
		String name = UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString();
		String ticketId = UUID.randomUUID().toString();
		AuthDTO token = new AuthDTO("userId", UUID.randomUUID().toString());

		when(authServiceMock.getToken(name, password)).thenReturn(ticketId);
		when(ticketServiceMock.waitForTicket(ticketId)).thenReturn(TicketStatusDTO.DONE);
		when(authResponseServiceMock.getToken(ticketId)).thenReturn(token);

		Future<AuthDTO> futureToken = client.getToken(name, password);
		assertEquals(token, futureToken.get());
	}


	@Test
	public void getServiceEndpoint() throws Exception {
		ServiceEndpointDTO endpoint = client.getServiceEndpoint();
		assertNotNull(endpoint);
		assertEquals(Protocol.DIRECT, endpoint.getProtocol());
	}
}
