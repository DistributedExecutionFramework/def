package at.enfilo.def.communication.impl.ticket;

import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Ticket Worker Thread
 */
public class TicketWorkerThread implements Runnable {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(TicketWorkerThread.class);

	private final Callable<Ticket<Object>> dataSupplier;
	private final Consumer<Ticket<Object>> statusUpdater;
	private boolean isInterrupted;

	public TicketWorkerThread(Callable<Ticket<Object>> dataSupplier, Consumer<Ticket<Object>> statusUpdater) {
		this.dataSupplier = dataSupplier;
		this.statusUpdater = statusUpdater;
		this.isInterrupted = false;
	}

	@Override
	public void run() {
		while (!isInterrupted) {
			try {
				// Fetch next ticket from queue
				Ticket<Object> nextTicket = dataSupplier.call();
				LOGGER.trace("Process next ticket {}.", nextTicket.getId());
				// Process ticket
				nextTicket.run();
				LOGGER.trace("Ticket {} processed.", nextTicket.getId());
				statusUpdater.accept(nextTicket); // update ticket status

			} catch (InterruptedException e) {
				isInterrupted = true;
				LOGGER.debug("TicketWorkerThread interrupted.");
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				LOGGER.error("Error while handling ticket.", e);
			}
		}
	}
}
