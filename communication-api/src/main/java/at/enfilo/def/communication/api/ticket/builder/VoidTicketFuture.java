package at.enfilo.def.communication.api.ticket.builder;

import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.TicketFailedException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Future implementation of a ticket. Returns ticket status with help of Future mechanism.
 */
class VoidTicketFuture extends AbstractTicketFuture<Void> {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(VoidTicketFuture.class);
	private static final String RESULT_AFTER_CANCELLATION_MESSAGE_FORMAT = "Result can not be requested after cancellation!";

	private TicketStatusDTO value;

	VoidTicketFuture(ITicketServiceClient ticketServiceClient, String ticketId) {
		super(ticketServiceClient, ticketId, LOGGER);
	}

	@Override
	protected boolean cancelInternalCommunication(boolean mayInterruptIfRunning) {
		//Future<TicketStatusDTO> ticketStatusFetcher = getTicketStatusFetcher();
		//return ticketStatusFetcher == null || ticketStatusFetcher.cancel(mayInterruptIfRunning) || ticketStatusFetcher.isCancelled();
		return true;
	}

	@Override
	public Void get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {

		if (isCancelled()) {
			throw new ExecutionException(new IllegalStateException(RESULT_AFTER_CANCELLATION_MESSAGE_FORMAT));
		}

		// Check if it is a first run.
		if (value != null) {
			return null;
		}

		// If timeout is negative - is a case of delegation from get() method.
		boolean isTimeLimited = timeout >= 0;

		// Waiting for ticketStatusFetcher finish.
		value = isTimeLimited ? getTicketStatusFetcher().get(timeout, timeUnit) : getTicketStatusFetcher().get();
		switch (value) {
			case DONE:
				return null;
			case FAILED:
				throw new TicketFailedException(getFailedMessage());
			default:
				throw new ExecutionException(new IllegalStateException(String.format(ILLEGAL_TICKET_STATUS_MESSAGE_FORMAT, value)));
		}
	}
}
