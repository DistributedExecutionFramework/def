package at.enfilo.def.communication.impl.ticket;

import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.api.ticket.rest.ITicketService;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ResponseException;

import java.util.UUID;

/**
 * Created by mase on 21.09.2016.
 */
public class TicketServiceImpl implements ITicketService {

	private final ITicketRegistry ticketRegistry;

	public TicketServiceImpl() {
		this(TicketRegistry.getInstance());
	}

	public TicketServiceImpl(ITicketRegistry ticketRegistry) {
		this.ticketRegistry = ticketRegistry;
	}

	@Override
	public TicketStatusDTO getTicketStatus(String ticketId) {
		return ticketRegistry.getTicketStatus(UUID.fromString(ticketId));
	}

	@Override
	public TicketStatusDTO waitForTicket(String ticketId) {
		try {
			return ticketRegistry.waitForTicket(UUID.fromString(ticketId));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ResponseException(e);
		}
	}

	@Override
	public TicketStatusDTO cancelTicketExecution(String ticketId, boolean mayInterruptIfRunning) {
		return ticketRegistry.cancelTicketExecution(UUID.fromString(ticketId), mayInterruptIfRunning);
	}

	@Override
	public String getFailedMessage(String ticketId) {
		return ticketRegistry.getFailedMessage(UUID.fromString(ticketId));
	}
}
