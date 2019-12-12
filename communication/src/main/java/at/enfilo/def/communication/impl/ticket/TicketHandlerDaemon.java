package at.enfilo.def.communication.impl.ticket;

import at.enfilo.def.common.util.DaemonThreadFactory;
import at.enfilo.def.communication.api.ITicketRegistryHandler;
import at.enfilo.def.communication.api.common.util.IHandlerDaemon;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicketHandlerDaemon implements IHandlerDaemon {

	private static final Logger LOGGER = LoggerFactory.getLogger(TicketHandlerDaemon.class);
	private static final Object INSTANCE_LOCK = new Object();

	private static TicketHandlerDaemon instance;

	private final ExecutorService executorServices;

	/**
	 * Starts TicketHandlerDaemon and initialize TicketRegistry.
	 * @param configuration - TicketHandler configuration
	 */
	public static void start(DEFTicketServiceConfiguration configuration) {
		synchronized (INSTANCE_LOCK) {
			if (instance == null) {
				instance = new TicketHandlerDaemon(configuration);
			}
		}
	}

	private TicketHandlerDaemon(DEFTicketServiceConfiguration configuration) {
		LOGGER.info("Start TicketHandlerDaemon with {} service threads.", configuration.getThreads());

		ITicketRegistryHandler ticketRegistry = TicketRegistry.initialize(configuration);

		// Start ticket working threads
		executorServices = Executors.newFixedThreadPool(
			configuration.getThreads(),
			r -> DaemonThreadFactory.newDaemonThread(r, "TicketHandler")
		);
		for (int i = 0; i < configuration.getThreads(); i++) {
			executorServices.submit(new TicketWorkerThread(
					ticketRegistry::nextTicket,
					ticketRegistry::touch
			));
		}

		// Shutdown hook for task handler daemon.
		Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownNow));
	}

	@Override
	public Collection<Runnable> shutdownNow() {
		LOGGER.info("Shutting down TicketHandlerDaemon.");
		return executorServices.shutdownNow();
	}
}
