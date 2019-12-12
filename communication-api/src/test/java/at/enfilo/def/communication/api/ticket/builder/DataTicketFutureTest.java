package at.enfilo.def.communication.api.ticket.builder;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.ticket.service.ITicketServiceClient;
import at.enfilo.def.communication.api.ticket.service.TicketServiceClientFactory;
import at.enfilo.def.communication.mock.ExecutorClientMock;
import at.enfilo.def.communication.mock.TicketServiceClientMock;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

/**
 * Created by mase on 28.09.2016.
 */
public class DataTicketFutureTest {
	private static final TimeUnit DUMMY_TIME_UNIT = TimeUnit.MILLISECONDS;

	private static final long FUTURE_NORMAL_TIMEOUT = 30000;
	private static final long FUTURE_SMALL_TIMEOUT = 1000;

	private static IClient<UUID> mockExecutorClient;
	private static TicketServiceClientMock mock;
	private static ITicketServiceClient client;

	@BeforeClass
	public static void setUp() throws Exception {
		mock = new TicketServiceClientMock();
		client = TicketServiceClientFactory.create(mock);
		mockExecutorClient = new ExecutorClientMock<>();
	}

	@Test
	public void getTest() throws Exception {
		Future<UUID> future = new DataTicketFuture<UUID, UUID>(
			client,
			mock.DONE_TICKET,
			(r, t) -> UUID.fromString(t),
			mockExecutorClient::execute
		);

		await().atMost(10, TimeUnit.SECONDS).until(future::isDone);
		assertEquals(mock.DONE_TICKET, future.get().toString());
	}

	@Test
	public void getWithTimeoutDoneTest() throws Exception {
		Future<UUID> future = new DataTicketFuture<UUID, UUID>(
			client,
			mock.DONE_TICKET,
			(r, t) -> UUID.fromString(t),
			mockExecutorClient::execute
		);

		assertEquals(TicketServiceClientMock.DONE_TICKET, future.get(FUTURE_NORMAL_TIMEOUT, DUMMY_TIME_UNIT).toString());
	}

	@Test(expected = TimeoutException.class)
	public void getWithTimeoutUnknownTest() throws Exception {
		Future<UUID> future = new DataTicketFuture<UUID, UUID>(
			client,
			mock.UNKNOWN_TICKET,
			(r, t) -> UUID.fromString(t),
			mockExecutorClient::execute
		);

		// This request should produce timeout exception independently from timeout value.
		future.get(mock.UNKNOWN_TICKET_SLEEP / 2, DUMMY_TIME_UNIT);
	}


	@Test(expected = ExecutionException.class)
	public void getWithTimeoutCanceledTest() throws Exception {
		Future<UUID> future = new DataTicketFuture<UUID, UUID>(
			client,
			mock.CANCELED_TICKET,
			(r, t) -> UUID.fromString(t),
			mockExecutorClient::execute
		);

		// This request should fail with execution exception (illegal state exception) independently from timeout value.
		// This test produces different exception due to the new, more efficient ticket status fetching mechanism.
		// As soon as "Canceled" status will be discovered, ticket status pooling will be stopped.
		future.get(FUTURE_NORMAL_TIMEOUT, DUMMY_TIME_UNIT);
	}

	@Test(expected = ExecutionException.class)
	public void cancelGetTest() throws Exception {
		Future<UUID> future = new DataTicketFuture<UUID, UUID>(
			client,
			mock.UNKNOWN_TICKET,
			(r, t) -> UUID.fromString(t),
			mockExecutorClient::execute
		);

		// Workaround to simulate cancel on pending get.
		Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Thread.sleep(FUTURE_SMALL_TIMEOUT);
				future.cancel(false);
				return null;
			}
		});

		await().atMost(10, TimeUnit.SECONDS).until(future::isCancelled);

		// This request should produce execution exception.
		future.get();
	}

	@Test(expected = ExecutionException.class)
	public void cancelGetTimeoutTest() throws Exception {
		Future<UUID> future = new DataTicketFuture<UUID, UUID>(
			client,
			mock.UNKNOWN_TICKET,
			(r, t) -> UUID.fromString(t),
			mockExecutorClient::execute
		);

		// Workaround to simulate cancel on pending get.
		Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Thread.sleep(FUTURE_SMALL_TIMEOUT);
				future.cancel(false);
				return null;
			}
		});

		await().atMost(10, TimeUnit.SECONDS).until(future::isCancelled);

		// This request should produce execution exception independently from timeout value.
		future.get(FUTURE_NORMAL_TIMEOUT, DUMMY_TIME_UNIT);
	}

	@Test(expected = ExecutionException.class)
	public void cancelInterruptGetTest() throws Exception {
		Future<UUID> future = new DataTicketFuture<UUID, UUID>(
			client,
			mock.UNKNOWN_TICKET,
			(r, t) -> UUID.fromString(t),
			mockExecutorClient::execute
		);

		// Workaround to simulate cancel on pending get.
		Executors.newSingleThreadExecutor().submit((Callable<Void>) () -> {
			Thread.sleep(FUTURE_SMALL_TIMEOUT);
			future.cancel(true);
			return null;
		});

		await().atMost(10, TimeUnit.SECONDS).until(future::isCancelled);

		// This request should produce execution exception.
		future.get();
	}

	@Test(expected = ExecutionException.class)
	public void cancelInterruptGetTimeoutTest() throws Exception {
		Future<UUID> future = new DataTicketFuture<UUID, UUID>(
			client,
			mock.UNKNOWN_TICKET,
			(r, t) -> UUID.fromString(t),
			mockExecutorClient::execute
		);

		// Workaround to simulate cancel on pending get.
		Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Thread.sleep(FUTURE_SMALL_TIMEOUT);
				future.cancel(true);
				return null;
			}
		});

		await().atMost(10, TimeUnit.SECONDS).until(future::isCancelled);

		// This request should produce execution exception independently from timeout value.
		future.get(FUTURE_NORMAL_TIMEOUT, DUMMY_TIME_UNIT);
	}


	@Test(expected = CancellationException.class)
	public void cancelGetWithoutIsCanceled() throws Exception {
		Future<UUID> future = new DataTicketFuture<UUID, UUID>(
				client,
				mock.UNKNOWN_TICKET,
				(r, t) -> UUID.fromString(t),
				mockExecutorClient::execute
		);

		// Workaround to simulate cancel on pending get.
		Executors.newSingleThreadExecutor().submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Thread.sleep(FUTURE_SMALL_TIMEOUT);
				future.cancel(true);
				return null;
			}
		});

		// This request should produce a cancellation exception
		future.get();
	}

	@Test(expected = ExecutionException.class)
	public void getFailedWithTimeout() throws Exception {
		Future<UUID> future = new DataTicketFuture<UUID, UUID>(
				client,
				mock.FAILED_TICKET,
				(r, t) -> UUID.fromString(t),
				mockExecutorClient::execute
		);

		// Produces ExecutionException
		future.get(FUTURE_NORMAL_TIMEOUT, DUMMY_TIME_UNIT);
	}

}
