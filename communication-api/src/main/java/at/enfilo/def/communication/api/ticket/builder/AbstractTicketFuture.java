package at.enfilo.def.communication.api.ticket.builder;

import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import org.slf4j.Logger;

import java.util.concurrent.*;

import static at.enfilo.def.communication.dto.TicketStatusDTO.CANCELED;
import static at.enfilo.def.communication.dto.TicketStatusDTO.UNKNOWN;
import static java.util.concurrent.TimeUnit.SECONDS;

public abstract class AbstractTicketFuture<V> implements Future<V> {

	static final ExecutorService executorService = Executors.newWorkStealingPool();

	protected static final String ILLEGAL_TICKET_STATUS_MESSAGE_FORMAT = "Illegal ticket status '%s' - expected 'DONE'.";
	protected static final String REQUEST_TIMEOUT_MESSAGE_FORMAT = "Request was not executed within %s %s.";
	protected static final String RESULT_AFTER_CANCELLATION_MESSAGE_FORMAT = "Result can not be requested after cancellation!";

	private final Logger logger;
	private final String ticketId;
	private final ITicketServiceClient ticketServiceClient;
	private Future<TicketStatusDTO> ticketStatusFetcher;

	private boolean isAlreadyCanceled = false;

	AbstractTicketFuture(
		ITicketServiceClient ticketServiceClient,
		String ticketId,
		Logger logger
	) {
		this.ticketId = ticketId;
		this.ticketServiceClient = ticketServiceClient;

		this.logger = logger;
	}

	protected abstract boolean cancelInternalCommunication(boolean mayInterruptIfRunning);

	protected String getTicketId() {
		return ticketId;
	}

	protected Future<TicketStatusDTO> getTicketStatusFetcher() {
		if (this.ticketStatusFetcher == null) {
			this.ticketStatusFetcher = executorService.submit(
					() -> ticketServiceClient.waitForTicket(ticketId)
			);
		}
		return ticketStatusFetcher;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (!isAlreadyCanceled) {

			TicketStatusDTO ticketStatus = UNKNOWN;
			try {
				ticketStatus = ticketServiceClient.cancelTicket(ticketId, mayInterruptIfRunning);
			} catch (ClientCommunicationException e) {
				logger.error("Communication error occurs while canceling a ticketId [Id: {}].", ticketId, e);
			}

			boolean isCanceledNowOrBefore = ticketStatus.equals(CANCELED) || ticketStatus.equals(UNKNOWN);
			boolean isInternalCommunicationCanceled = cancelInternalCommunication(mayInterruptIfRunning);
			boolean isTicketStatusFetchThreadCanceled = ticketStatusFetcher == null || getTicketStatusFetcher().cancel(mayInterruptIfRunning) || getTicketStatusFetcher().isCancelled();

			isAlreadyCanceled = isCanceledNowOrBefore && isTicketStatusFetchThreadCanceled && isInternalCommunicationCanceled;
		}

		return isAlreadyCanceled;
	}

	@Override
	public boolean isCancelled() {
		return isAlreadyCanceled;
	}

	@Override
	public boolean isDone() {
		// See javadoc for isDone method.
		return getTicketStatusFetcher().isDone() || isCancelled();
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		try {
			return get(-1, SECONDS);
		} catch (TimeoutException e) {
			throw new ExecutionException(e);
		}
	}

	/**
	 * Returns failed message of a ticket
	 */
	protected String getFailedMessage() {
		try {
			return ticketServiceClient.getFailedMessage(ticketId);
		} catch (ClientCommunicationException e) {
			logger.error("Error while fetch failed message of ticket id {}.", ticketId, e);
		}
		return null;
	}
}