package at.enfilo.def.communication.api;

import at.enfilo.def.common.api.ITouchable;
import at.enfilo.def.communication.api.common.util.IRegistryHandler;
import at.enfilo.def.communication.impl.ticket.Ticket;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public interface ITicketRegistryHandler extends IRegistryHandler, ITouchable<Ticket<?>> {
	/**
	 * Cleanup TicketRegistry: Removes all Tickets which are older than given time.
	 * @param olderThan - time value
	 * @param unit - time unit
	 */
	void cleanup(long olderThan, @Nonnull TimeUnit unit);

	/**
	 * Returns next ticket in queue for execution.
	 * @return - next Ticket in queue
	 */
	Ticket nextTicket() throws InterruptedException;
}
