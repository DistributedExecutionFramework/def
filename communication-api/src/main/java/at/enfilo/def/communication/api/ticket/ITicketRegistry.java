package at.enfilo.def.communication.api.ticket;

import at.enfilo.def.common.api.IVoidCallable;
import at.enfilo.def.communication.dto.TicketStatusDTO;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * TicketRegistry Interface.
 *
 * The TicketRegistry is responsible for managing all created tickets.
 */
public interface ITicketRegistry {

	/**
	 * Creates a normal ticket for business logic operation.
	 * @param resultClass - class of result type
	 * @param operation - operation(s), which are executed by this this ticket
	 * @param <T> - type of result
	 * @return - Ticket
	 */
	<T> ITicket<T> createTicket(Class<T> resultClass, Callable<T> operation);
	<T> ITicket<T> createTicket(Class<T> resultClass, Callable<T> operation, byte priority);

	/**
	 * Creates a normal ticket for business logic operation with no return.
	 * @param operation - operation(s), which are executed by this this ticket
	 * @return - Ticket
	 */
	ITicket<Void> createTicket(IVoidCallable operation);
	ITicket<Void> createTicket(IVoidCallable operation, byte priority);

	/**
	 * Create a "already failed ticket".
	 * @param resultClass - result class type
	 * @param <T> - result type
 *     @param message - failed message
	 * @return a ticket with state failed
	 */
	<T> ITicket<T> createFailedTicket(Class<T> resultClass, String message);
	ITicket<Void> createFailedTicket(String message);


	/**
	 * Returns the state of the given ticket.
	 * @param ticketId - id of requested ticket
	 * @return - {@link TicketStatusDTO}.UNKNOWN means that the ticket is not known/avail.
	 */
	TicketStatusDTO getTicketStatus(UUID ticketId);


	/**
	 * Returns and <b>removes</b> the given ticket from TicketRegistry.
	 * @param ticketId - requested ticket
	 * @return - requested ticket
	 */
	ITicket withdrawTicket(UUID ticketId);

	/**
	 * Returns and <b>removes</b> the given ticket from TicketRegistry.
	 * @param ticketId - requested ticket
	 * @param <T> ticket type
	 * @param <R> - return type (result class)
	 * @return - requested ticket
	 */
	<T extends ITicket<R>, R> T withdrawTicket(UUID ticketId, Class<R> resultClass) throws ExecutionException;

	/**
	 * Cancel the execution of a ticket.
	 * @param ticketId - id of ticket
	 * @param mayInterruptIfRunning - interrupt or not
	 * @return new status (CANCELED)
	 */
	TicketStatusDTO cancelTicketExecution(UUID ticketId, boolean mayInterruptIfRunning);

	/**
	 * Wait for ticket reaching state SUCCESS or FAILED.
	 * @param ticketId - id of ticket
	 * @return final ticket status
	 */
	TicketStatusDTO waitForTicket(UUID ticketId) throws InterruptedException;

	/**
	 * Returns failed message of a ticket.
	 * @param ticketId - id of ticket
	 * @return failed message / stack trace
	 */
	String getFailedMessage(UUID ticketId);
}
