package at.enfilo.def.communication.impl.ticket;

import at.enfilo.def.common.api.ITimeoutMap;
import at.enfilo.def.common.api.IVoidCallable;
import at.enfilo.def.common.exception.NotInitializedException;
import at.enfilo.def.common.exception.UnexpectedConditionException;
import at.enfilo.def.common.impl.TimeoutMap;
import at.enfilo.def.communication.api.ITicketRegistryHandler;
import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.NotificationEmitter;
import java.lang.management.*;
import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


/**
 * Real TicketRegistry implementation (ITicketRegistry, ITicketRegistryHandler interfaces).
 * Handles all tickets: creating, storing, cleaning.
 */
public class TicketRegistry implements ITicketRegistry, ITicketRegistryHandler {

	/**
	 * Task which checks ticket queue size periodically on low on memory detection.
	 */
	private class PeriodicQueueChecker extends TimerTask {
		private long lastQueueSizeSnapshot;

		private PeriodicQueueChecker() {
			lastQueueSizeSnapshot = queueSizeSnapshot;
		}

		@Override
		public void run() {
			if (!memoryPoolMXBean.isCollectionUsageThresholdExceeded()) {
				periodicMemoryCheckTimer.cancel();
				periodicMemoryCheckTimer = null;
				lowOnMemory.set(false);
				LOGGER.info(
						"Memory usage changed back to normal. Tickets will be accepted without delay. Usage: {}",
						memoryPoolMXBean.getCollectionUsage()
				);
				return;
			}
			queueSizeSnapshot = queue.size();
			if (queueSizeSnapshot > configuration.getThreads()) {
				long diff = lastQueueSizeSnapshot - queueSizeSnapshot - configuration.getThreads();
				if (diff <= 0) {
					delayOnTicketAccept.addAndGet(100);
					LOGGER.info(
							"Ticket queue grows instead of shrink. Increased Ticked accept delay to {}ms. Ticket queue size: {}.",
							delayOnTicketAccept,
							queueSizeSnapshot
					);
				}
			}
			lastQueueSizeSnapshot = queueSizeSnapshot;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(TicketRegistry.class);

	private static final int VALUE_EXPIRATION_DELAY = 5 * 60;
	private static final TimeUnit VALUE_EXPIRATION_UNIT = TimeUnit.MINUTES;
	private static final int CLEAN_SCHEDULE_DELAY = 5 * 60 / 2;
	private static final TimeUnit CLEAN_SCHEDULE_UNIT = TimeUnit.MINUTES;

	private static TicketRegistry instance;

	private final PriorityBlockingQueue<Ticket> queue;
	private final ITimeoutMap<UUID, Ticket> registry;
	private final DEFTicketServiceConfiguration configuration;
	private final AtomicBoolean lowOnMemory;
	private final AtomicLong delayOnTicketAccept;
	private MemoryPoolMXBean memoryPoolMXBean;
	private Timer periodicMemoryCheckTimer;
	private long queueSizeSnapshot;

	/**
	 * Singleton - returns a instance.
	 * @return
	 */
	public static ITicketRegistry getInstance() {
		if (instance == null) {
			throw new NotInitializedException("TicketRegistry must be initialized first: TicketHandlerDaemon.start()");
		}
		return instance;
	}

	/**
	 * Initialize TicketRegistry
	 * @param configuration
	 * @return
	 */
	static TicketRegistry initialize(DEFTicketServiceConfiguration configuration) {
		if (instance == null) {
			instance = new TicketRegistry(configuration);
		}
		return instance;
	}

	TicketRegistry(DEFTicketServiceConfiguration configuration) {
		LOGGER.info("Initialize TicketRegistry.");
		this.queue = new PriorityBlockingQueue<>();
		this.registry = new TimeoutMap<>(VALUE_EXPIRATION_DELAY, VALUE_EXPIRATION_UNIT, CLEAN_SCHEDULE_DELAY, CLEAN_SCHEDULE_UNIT);
		this.configuration = configuration;
		this.lowOnMemory = new AtomicBoolean(false);
		this.delayOnTicketAccept = new AtomicLong(100);

		// Memory usage listener
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		NotificationEmitter emitter = (NotificationEmitter) memoryMXBean;
		emitter.addNotificationListener((notification, handBack) -> {
			String notifyType = notification.getType();
			if (notifyType.equals(MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED)) {
				lowOnMemory.set(true);
				if (periodicMemoryCheckTimer != null) {
					delayOnTicketAccept.addAndGet(100);
					LOGGER.info(
							"Memory threshold exceeded. Increased Ticked accept delay to {}ms.",
							delayOnTicketAccept
					);
				} else {
					delayOnTicketAccept.set(100); // initial value: 100ms
					periodicMemoryCheckTimer = new Timer();
					periodicMemoryCheckTimer.scheduleAtFixedRate(
							new PeriodicQueueChecker(),
							configuration.getMemoryCheckInterval(),
							configuration.getMemoryCheckInterval()
					);
					LOGGER.info(
							"Memory threshold exceeded. Accept of Tickets with normal or lower priority will be slowed down until memory is recovered. Usage: {}",
							memoryPoolMXBean.getCollectionUsage()
					);
				}
			}
		}, null, null);

		// Set threshold on memory pools
		for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
			if (p.isUsageThresholdSupported() && (p.getType() == MemoryType.HEAP)) {
				long threshold = (long)(p.getUsage().getMax() * configuration.getMemoryThreshold());
				p.setCollectionUsageThreshold(threshold);
				memoryPoolMXBean = p;
				break;
			}
		}
	}

	@Override
	public <T> ITicket<T> createTicket(Class<T> resultClass, Callable<T> operation) {
		return createTicket(resultClass, operation, ITicket.NORMAL_PRIORITY);
	}

	@Override
	public <T> ITicket<T> createTicket(Class<T> resultClass, Callable<T> operation, byte priority) {
		LOGGER.trace("Create new Ticket<{}>[prio={}] for Operation {}", resultClass, priority, operation);
		Ticket<T> ticket = new Ticket<>(resultClass, operation, priority);
		registerAndQueueTicket(ticket);
		return ticket;
	}

	@Override
	public ITicket<Void> createTicket(IVoidCallable operation) {
		return createTicket(operation, ITicket.NORMAL_PRIORITY);
	}

	@Override
	public ITicket<Void> createTicket(IVoidCallable operation, byte priority) {
		LOGGER.trace("Create new Ticket<Void>[prio={}] for Operation {}", priority, operation);
		Ticket<Void> ticket = new Ticket<>(Void.class, operation.toCallable(), priority);
		registerAndQueueTicket(ticket);
		return ticket;
	}

	private void registerAndQueueTicket(Ticket<?> ticket) {
		if (ticket.getPriority() >= ITicket.NORMAL_PRIORITY) {
			if (lowOnMemory.get()) {
				try {
					Thread.sleep(delayOnTicketAccept.get());
				} catch (InterruptedException e) {
					LOGGER.error("Error while delay ticket registration on low memory.", e);
					Thread.currentThread().interrupt();
				}
			}
		}
		registry.put(ticket.getId(), ticket);
		queue.add(ticket);
	}

	@Override
	public Ticket nextTicket() throws InterruptedException {
		Ticket next = queue.take();
		LOGGER.trace("Picked next Ticket with id {} for processing", next.getId());
		return next;
	}

	@Override
	public ITicket withdrawTicket(UUID ticketId) {
		LOGGER.trace("Get and remove Ticket {} from registry", ticketId);
		return registry.remove(ticketId);
	}

	@Override
	public <T extends ITicket<R>, R> T withdrawTicket(UUID ticketId, Class<R> resultClass)
	throws ExecutionException {
		try {
			ITicket<?> ticket = withdrawTicket(ticketId);
			if (ticket != null) {
				if (ticket.getResultClass().isAssignableFrom(resultClass)) {
					@SuppressWarnings("unchecked") T tTicket = (T) ticket;
					return tTicket;
				} else {
					LOGGER.error("Requested ticket was associated with different return type. Requested {}, assigned {}.", resultClass.getSimpleName(), ticket.getResultClass());
					throw new UnexpectedConditionException("Requested ticket was associated with different return type.");
				}
			} else {
				LOGGER.error("Requested ticket {} already removed.", ticketId.toString());
				throw new UnknownTicketException("Ticket already removed.");
			}

		} catch (UnexpectedConditionException | UnknownTicketException e) {
			throw new ExecutionException(e);
		}
	}

	@Override
	public TicketStatusDTO getTicketStatus(UUID ticketId) {
		if (registry.containsKey(ticketId)) {
			return registry.get(ticketId).getStatus();
		}
		return TicketStatusDTO.UNKNOWN;
	}

	@Override
	public TicketStatusDTO waitForTicket(UUID ticketId) throws InterruptedException {
		if (registry.containsKey(ticketId)) {
			ITicket ticket = registry.get(ticketId);
			ticket.waitForComplete();
			return ticket.getStatus();
		}
		return TicketStatusDTO.UNKNOWN;
	}

	@Override
	public void cleanup(long olderThan, TimeUnit timeUnit) {
		LOGGER.info("Cleanup all finished (done, canceled, failed) tickets which are not requested and older than {} {}", olderThan, timeUnit);
		Instant limit = Instant.now().minusNanos(timeUnit.toNanos(olderThan));
		for (Ticket<?> t : registry.values()) {
			switch (t.getStatus()) {
				case DONE:
				case FAILED:
				case CANCELED:
					if (t.getFinishedTime().isAfter(limit)) {
						registry.remove(t.getId());
					}
					break;
			}
		}
	}

	@Override
	public TicketStatusDTO cancelTicketExecution(UUID ticketId, boolean mayInterruptIfRunning) {
		LOGGER.debug("Try to cancel Ticket {}", ticketId);
		if (registry.containsKey(ticketId)) {
			Ticket ticket = registry.remove(ticketId);
			return ticket.cancel(mayInterruptIfRunning);
		}
		return TicketStatusDTO.UNKNOWN;
	}

	@Override
	public boolean touch(Ticket<?> ticket) {
		return registry.touch(ticket.getId());
	}

	@Override
	public <T> ITicket<T> createFailedTicket(Class<T> resultClass, String message) {
		LOGGER.trace("Create new already failed Ticket<{}>.", resultClass);
		Ticket<T> failedTicket = new Ticket<>(resultClass, () -> {return null;}, ITicket.NORMAL_PRIORITY);
		failedTicket.cancel(true);
		registry.put(failedTicket.getId(), failedTicket);
		return failedTicket;
	}

	@Override
	public ITicket<Void> createFailedTicket(String message) {
		return createFailedTicket(Void.class, message);
	}

	@Override
	public String getFailedMessage(UUID ticketId) {
		String failedMessage = null;
		if (registry.containsKey(ticketId)) {
			failedMessage = registry.get(ticketId).getException();
		}
		return failedMessage == null ? "" : failedMessage;
	}
}
