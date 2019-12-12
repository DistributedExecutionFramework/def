package at.enfilo.def.communication.api.ticket.service;

import at.enfilo.def.communication.api.ticket.rest.ITicketService;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class TicketServiceClientTest {
	private ITicketServiceClient client;
	private ITicketService ticketServiceMock;

	@Before
	public void setUp() throws Exception {
		ticketServiceMock = Mockito.mock(ITicketService.class);

		client = TicketServiceClientFactory.createDirectClient(ticketServiceMock);
	}

	@Test
	public void getTicketStatus() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		TicketStatusDTO status = TicketStatusDTO.IN_PROGRESS;

		when(ticketServiceMock.getTicketStatus(ticketId)).thenReturn(status);

		TicketStatusDTO requestedStatus = client.getTicketStatus(ticketId);
		assertEquals(status, requestedStatus);
	}

	@Test
	public void cancelTicketExecution() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		boolean mayInterrupt = true;

		when(ticketServiceMock.cancelTicketExecution(ticketId, mayInterrupt)).thenReturn(TicketStatusDTO.CANCELED);

		TicketStatusDTO canceled = client.cancelTicket(ticketId, mayInterrupt);
		assertEquals(TicketStatusDTO.CANCELED, canceled);
	}

	@Test
	public void getFailedMessage() throws Exception {
		String ticketId = UUID.randomUUID().toString();
		String failedMessage = UUID.randomUUID().toString();

		when(ticketServiceMock.getFailedMessage(ticketId)).thenReturn(failedMessage);

		assertEquals(failedMessage, client.getFailedMessage(ticketId));
	}
}
