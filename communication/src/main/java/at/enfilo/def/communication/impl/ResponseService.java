package at.enfilo.def.communication.impl;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.exception.ResponseException;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public abstract class ResponseService {
	private Logger logger;
	private ITicketRegistry ticketRegistry;

	protected ResponseService(Logger logger, ITicketRegistry ticketRegistry) {
		this.logger = logger;
		this.ticketRegistry = ticketRegistry;
	}

	protected ResponseService(Logger logger) {
		this(logger, TicketRegistry.getInstance());
	}

    /**
     * Getter for internally used TicketRegistry impl.
     * @return instance of TicketRegistry.
     */
	protected ITicketRegistry getTicketRegistry() {
        return ticketRegistry;
    }

	/**
	 * Returns result (casted object) of the given ticket.
	 *
	 * @param ticketId - ticket id
	 * @param resultClass - class of result object
	 * @param <R> - type of result
	 * @return result object
	 * @throws ResponseException if a Future error occurred
	 */
	protected <R> R getResult(String ticketId, Class<R> resultClass) throws ResponseException {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		String callerMethodName = stackTraceElements.length >= 3 ? stackTraceElements[2].getMethodName() : stackTraceElements[1].getMethodName();

		logger.debug("getResult() for Method {}", callerMethodName);
		try {
			ITicket<R> ticket = getTicketRegistry().withdrawTicket(UUID.fromString(ticketId), resultClass);
			R result = ticket.getResult();
			ticket.clean();
			return result;

		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error while waiting for result of ticket [tId: {}], Method \"{}\".", ticketId, callerMethodName, e);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			throw new ResponseException(e);
		}
	}
}
