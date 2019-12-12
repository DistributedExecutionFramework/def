package at.enfilo.def.communication.impl.ticket;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;


public class TicketRegistryAndTicketHandlerTest {

	private static ITicketRegistry registry;

	@BeforeClass
	public static void setUp() throws Exception {
		TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());
		registry = TicketRegistry.getInstance();
	}


	@Test
	public void handleNormalTicket() throws Exception {
		int processTime = 1000;
		String ticketResult = UUID.randomUUID().toString();

		ITicket<String> ticket = registry.createTicket(
				String.class,
				() -> {
					Thread.sleep(processTime);
					return ticketResult;
				}
		);

		assertEquals(TicketStatusDTO.IN_PROGRESS, ticket.getStatus());

		await()
				.atLeast(processTime / 2, TimeUnit.MILLISECONDS)
				.and()
				.atMost(processTime * 2, TimeUnit.MILLISECONDS)
				.until(() -> ticket.getStatus() == TicketStatusDTO.DONE);

		assertEquals(ticketResult, ticket.getResult());
	}

	@Test
	public void handleServiceTicket() throws Exception {
		int processTime = 1000;
		String ticketResult = UUID.randomUUID().toString();

		ITicket<String> ticket = registry.createTicket(
				String.class,
				() -> {
					Thread.sleep(processTime);
					return ticketResult;
				},
				ITicket.SERVICE_PRIORITY
		);

		await()
				.atLeast(processTime / 2, TimeUnit.MILLISECONDS)
				.and()
				.atMost(processTime * 2, TimeUnit.MILLISECONDS)
				.until(() -> ticket.getStatus() == TicketStatusDTO.DONE);

		assertEquals(ticketResult, ticket.getResult());
	}

	@Test
	public void cancelTicket() throws Exception {
		int processTime = 1000;
		String ticketResult = UUID.randomUUID().toString();

		ITicket<String> ticket = registry.createTicket(
				String.class,
				() -> {
					for (int i = 0; i < 100; i++) {
						Thread.sleep(processTime);
					}
					return ticketResult;
				}
		);

		TicketStatusDTO cancel = ticket.cancel(true);
		assertEquals(TicketStatusDTO.CANCELED, cancel);
	}

	@Test
	public void cancelTicketAndNormalTicket() throws Exception {
		for (int i = 0; i < 10; i++) {
			cancelTicket();
		}
		for (int i = 0; i < 10; i++) {
			handleNormalTicket();
		}
	}

	@Test
	public void failedTicket() throws Exception {
		int processTime = 1000;
		String exceptionMessage = UUID.randomUUID().toString();

		ITicket<String> ticket = registry.createTicket(
				String.class,
				() -> {
					Thread.sleep(processTime);
					throw new RuntimeException(exceptionMessage);
				}
		);

		await()
				.atLeast(processTime / 2, TimeUnit.MILLISECONDS)
				.and()
				.atMost(processTime * 2, TimeUnit.MILLISECONDS)
				.until(() -> ticket.getStatus() == TicketStatusDTO.FAILED);

		assertNotNull(ticket.getException());
		assertTrue(ticket.getException().contains(exceptionMessage));
	}
}
