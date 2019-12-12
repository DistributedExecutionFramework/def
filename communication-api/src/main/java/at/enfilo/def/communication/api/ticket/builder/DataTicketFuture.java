package at.enfilo.def.communication.api.ticket.builder;

import at.enfilo.def.common.api.IThrowingBiFunction;
import at.enfilo.def.common.api.IThrowingFunction;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.TicketFailedException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Future implementation of a ticket. Returns data with help of Future mechanism.
 *
 * @param <T> - Client Type
 * @param <V> - Value Type
 */
class DataTicketFuture<T, V> extends AbstractTicketFuture<V> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(DataTicketFuture.class);

    private final Callable<V> callable;
    private final ITicketServiceClient ticketServiceClient;

    private V value;
    private Future<V> responseFetcher;

    DataTicketFuture(
        ITicketServiceClient ticketServiceClient,
        String ticketId,
        IThrowingBiFunction<T, String, V> subjectProxyFunction,
        IThrowingFunction<IThrowingFunction<T, V>, V> executorFunction
    ) throws IllegalArgumentException {
    	super(ticketServiceClient, ticketId, LOGGER);

    	this.ticketServiceClient = ticketServiceClient;
        this.callable = () -> executorFunction.apply(client -> subjectProxyFunction.apply(client, ticketId));
    }

	@Override
	protected boolean cancelInternalCommunication(boolean mayInterruptIfRunning) {
		return responseFetcher == null || responseFetcher.cancel(mayInterruptIfRunning) || responseFetcher.isCancelled();
	}

	@Override
    public V get(long timeout, TimeUnit timeUnit)
    throws InterruptedException, ExecutionException, TimeoutException {

		// Check if it is a first run.
		if (value != null) {
			return value;
		}

		if (isCancelled()) {
			throw new ExecutionException(new IllegalStateException(RESULT_AFTER_CANCELLATION_MESSAGE_FORMAT));
		}

		// If timeout is negative - is a case of delegation from get() method.
		boolean isTimeLimited = timeout >= 0;

		long timeoutLeft = timeout;
		TicketStatusDTO ticketStatus;

		// First: Fetch ticket status
		if (isTimeLimited) {
			// Storing start time to handle timeout functionality.
			Instant startTime = Instant.now();

			// Waiting for ticketStatusFetcher finish.
			ticketStatus = getTicketStatusFetcher().get(timeout, timeUnit);

			// Performing check if we are still in timeout bound.
			Duration executionDuration = Duration.between(startTime, Instant.now());
			Duration leftDuration = Duration.ofNanos(timeUnit.toNanos(timeout)).minus(executionDuration);
			if (leftDuration.isNegative() || leftDuration.isZero()) {
				throw new TimeoutException(String.format(
						REQUEST_TIMEOUT_MESSAGE_FORMAT,
						timeout,
						timeUnit
				));
			}

			// Updating timeout left value.
			timeoutLeft = timeUnit.convert(leftDuration.toNanos(), NANOSECONDS);
		} else {
			// Waiting for ticketStatusFetcher finish.
			ticketStatus = getTicketStatusFetcher().get();
		}

		// Second: fetch ticket data
		switch (ticketStatus) {
			case DONE:
				// This point is reached only if ticket waiting task has succeeded and we still have time left or it is not a timed request.
				responseFetcher = executorService.submit(callable);
				value = isTimeLimited ? responseFetcher.get(timeoutLeft, timeUnit) : responseFetcher.get();
				return value;
			case FAILED:
				throw new TicketFailedException(getFailedMessage());
			default:
				throw new ExecutionException(new IllegalStateException(String.format(ILLEGAL_TICKET_STATUS_MESSAGE_FORMAT, ticketStatus)));
		}
	}
 }
