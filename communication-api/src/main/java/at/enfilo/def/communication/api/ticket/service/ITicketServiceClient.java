package at.enfilo.def.communication.api.ticket.service;

import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;

/**
 * Client interface for TicketService.
 */
public interface ITicketServiceClient {
	/**
	 * Returns TicketStatus for requested ticket.
	 * @param ticketId - if of ticket
	 * @return current state of ticket
	 */
	TicketStatusDTO getTicketStatus(String ticketId) throws ClientCommunicationException;

	/**
	 * Wait for ticket is finished.
	 * @param ticketId - id of ticket
	 * @return final state of ticket
	 */
	TicketStatusDTO waitForTicket(String ticketId) throws ClientCommunicationException;

	/**
	 * Cancel requested ticket
	 * @param ticketId - id of ticket
	 * @return final state of ticket (canceled)
	 */
	TicketStatusDTO cancelTicket(String ticketId, boolean mayInterruptIfRunning) throws ClientCommunicationException;

	/**
	 * Returns failed message of a ticket
	 * @param ticketId - iid of ticket
	 * @return failed message if available, otherwise an empty string
	 */
	String getFailedMessage(String ticketId) throws ClientCommunicationException;

	/**
	 * Close client.
	 */
	void close();
}
