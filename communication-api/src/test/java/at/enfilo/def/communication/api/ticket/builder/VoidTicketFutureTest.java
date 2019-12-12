package at.enfilo.def.communication.api.ticket.builder;

import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.api.ticket.service.TicketServiceClientFactory;
import at.enfilo.def.communication.mock.TicketServiceClientMock;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;

public class VoidTicketFutureTest {

	private static TicketServiceClientMock mock;
	private static ITicketServiceClient client;

	@BeforeClass
	public static void setUp() throws Exception {
		mock = new TicketServiceClientMock();
		client = TicketServiceClientFactory.create(mock);
	}

	@Test
	public void isDone() throws Exception {
		Future<Void> tDone = new VoidTicketFuture(client, mock.DONE_TICKET);
		await().atMost(5, TimeUnit.SECONDS).until(tDone::isDone);
		assertTrue(tDone.isDone());

		Future<Void> tCanceled = new VoidTicketFuture(client, mock.CANCELED_TICKET);
		await().atMost(5, TimeUnit.SECONDS).until(tCanceled::isDone);
		assertTrue(tCanceled.isDone());

		Future<Void> tInProgress = new VoidTicketFuture(client, mock.IN_PROGRESS_TICKET);
		await().atMost(5, TimeUnit.SECONDS).until(tInProgress::isDone);
		assertTrue(tInProgress.isDone());

		Future<Void> tUnknown = new VoidTicketFuture(client, mock.UNKNOWN_TICKET);
		await().atMost(5, TimeUnit.SECONDS).until(() -> !tUnknown.isDone());
		assertFalse(tUnknown.isDone());

		Future<Void> tFailed = new VoidTicketFuture(client, mock.FAILED_TICKET);
		await().atMost(5, TimeUnit.SECONDS).until(tFailed::isDone);
		assertTrue(tFailed.isDone());
	}

	@Test
	public void get() throws Exception {
		Future<Void> ticket = new VoidTicketFuture(client, mock.DONE_TICKET);
		assertNull(ticket.get());
	}

	@Test(expected = ExecutionException.class)
	public void getFailed() throws Exception {
		Future<Void> ticket = new VoidTicketFuture(client, mock.FAILED_TICKET);
		ticket.get();
	}

	@Test
	public void cancel() throws Exception {
		// Cancel a ticket with unknown state
		Future<Void> tUnknown = new VoidTicketFuture(client, mock.UNKNOWN_TICKET);
		boolean cancelResult = tUnknown.cancel(true);
		assertTrue(cancelResult);
		assertTrue(tUnknown.isCancelled());

		// Cancel a ticket with in progress state
		Future<Void> tInProgress = new VoidTicketFuture(client, mock.IN_PROGRESS_TICKET);
		cancelResult = tInProgress.cancel(true);
		assertTrue(cancelResult);
		assertTrue(tInProgress.isCancelled());

		// Cancel a "done" ticket --> should return false
		Future<Void> tDone = new VoidTicketFuture(client, mock.DONE_TICKET);
		tDone.get(); // get info that is already done.
		cancelResult = tDone.cancel(true);
		assertFalse(cancelResult);
		assertFalse(tDone.isCancelled());

		// Cancel a already canceled ticket --> should return true
		Future<Void> tCanceled = new VoidTicketFuture(client, mock.CANCELED_TICKET);
		tCanceled.cancel(true);
		try {
			tCanceled.get(); // get info that is already canceled.
			fail();
		} catch (ExecutionException e) {
			// Expected
		}
		// above line causes test to fail
		// (as well as new ticket pulling mechanism - ticket pulling stops and will be "Done" as soon as it receives status "Canceled", "Failed" or "Done")
		// while both cases are expected due to the javadoc specification.
		// after task isDone() - method isCanceled will always return false.
		// continuous cancel() and isCanceled() calls - will provide true if true was returned after the first execution.
		// refactored to be functional.
		cancelResult = tCanceled.cancel(true);
		assertTrue(cancelResult);
		cancelResult = tCanceled.cancel(true);
		assertTrue(cancelResult); // due to javadoc for cancel()
		assertTrue(tCanceled.isCancelled());
	}

	@Test
	public void getWithTimeout() throws Exception {
		Future<Void> tChange = new VoidTicketFuture(client, mock.LONG_WAITING_TICKET);
		tChange.get(mock.LONG_WAITING * 10, TimeUnit.SECONDS);
	}

	@Test(expected = TimeoutException.class)
	public void getWithTimeoutException() throws Exception {
		Future<Void> tChange = new VoidTicketFuture(client, mock.LONG_WAITING_TICKET);
		tChange.get(mock.LONG_WAITING / 10, TimeUnit.SECONDS);
	}
}
