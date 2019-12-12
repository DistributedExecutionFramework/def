package at.enfilo.def.communication.impl.ticket;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class TicketRegistryTest {

	private TicketRegistry ticketRegistry;

	@Before
	public void setUp() throws Exception {
		ticketRegistry = new TicketRegistry(DEFTicketServiceConfiguration.getDefault());
	}

	@Test
	public void fifoTickets() throws Exception {
		List<ITicket<Void>> tickets = new LinkedList<>();
		Random rnd = new Random();
		int nrOfTickets = rnd.nextInt(1000) + 100;
		for (int i = 0; i < nrOfTickets; i++) {
			ITicket<Void> t = ticketRegistry.createTicket(() -> {});
			tickets.add(t);
		}

		for (ITicket<Void> t : tickets) {
			ITicket<Void> next = ticketRegistry.nextTicket();
			assertEquals(t, next);
		}
	}

	@Test
	public void priorityTickets() throws InterruptedException {
		ITicket<Void> t1 = ticketRegistry.createTicket(() -> {}, (byte)0x20);
		ITicket<Void> t2 = ticketRegistry.createTicket(() -> {}, (byte)0x30);
		ITicket<Void> t3 = ticketRegistry.createTicket(() -> {}, (byte)0x10);
		ITicket<Void> t4 = ticketRegistry.createTicket(() -> {}, (byte)0x00);
		ITicket<Void> t5 = ticketRegistry.createTicket(() -> {}, (byte)0x40);
		List<ITicket<Void>> tickets = new LinkedList<>();
		tickets.add(t4);
		tickets.add(t3);
		tickets.add(t1);
		tickets.add(t2);
		tickets.add(t5);

		for (ITicket<Void> t : tickets) {
			ITicket<Void> next = ticketRegistry.nextTicket();
			assertEquals(t, next);
		}
	}
}
