package at.enfilo.def.communication.impl.ticket;

import at.enfilo.def.communication.api.ticket.ITicket;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import static at.enfilo.def.communication.dto.TicketStatusDTO.*;

public class Ticket<T> implements ITicket<T>, Comparable<Ticket<T>>, Runnable {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(Ticket.class);

	private static final AtomicLong SEQ = new AtomicLong(0);

	private final UUID id;
	private final Class<T> resultClass;
	private final CountDownLatch resultSignal;
	private final long seq;
	private final byte priority;
	private final Object stateAndTimeLock;

	private Callable<T> operation;
	private T result;
	private Instant finishedTime;
	private String exception;
	private TicketStatusDTO state;
	private Thread runningThread;

	public Ticket(Class<T> resultClass, Callable<T> operation, byte priority) {
		this.id = UUID.randomUUID();
		this.seq = SEQ.getAndIncrement();
		this.priority = priority;
		this.resultClass = resultClass;
		this.operation = operation;
		this.resultSignal = new CountDownLatch(1);
		this.stateAndTimeLock = new Object();
		this.state = TicketStatusDTO.IN_PROGRESS;
	}

	@Override
    public UUID getId() {
        return id;
    }

    @Override
    public Class<T> getResultClass() {
        return resultClass;
    }

    @Override
    public TicketStatusDTO getStatus() {
		synchronized (stateAndTimeLock) {
			return state;
		}
	}

	@Override
	public String getException() {
		return exception;
	}

	@Override
	public void run() {
		LOGGER.trace("Run ticket {}.", id);
		synchronized (stateAndTimeLock) {
			if (state == CANCELED || state == FAILED || state == DONE) {
				LOGGER.warn("Can not run ticket {} because it is in state {}.", id, state);
				return;
			}
		}
		runningThread = Thread.currentThread();
		try {
			result = operation.call();
			LOGGER.trace("Ticket {} done.", id);
			synchronized (stateAndTimeLock) {
				state = TicketStatusDTO.DONE;
				finishedTime = Instant.now();
			}
		} catch (InterruptedException e) {
			LOGGER.debug("Ticket {} interrupted/canceled.", id, e);
			synchronized (stateAndTimeLock) {
				state = CANCELED;
			}
		} catch (Exception e) {
			LOGGER.error("Ticket {} failed.", id, e);
			StringWriter sw = new StringWriter();
			try (PrintWriter pw = new PrintWriter(sw)) {
				e.printStackTrace(pw);
				if (e.getCause() != null) {
					e.getCause().printStackTrace(pw);
				}
			}
			StringBuilder sb = new StringBuilder();
			sb.append('\n');
			for (String exLine : sw.toString().split(System.lineSeparator())) {
				sb.append('>').append(' ').append(exLine).append('\n');
			}
			exception = sb.toString();
			synchronized (stateAndTimeLock) {
				state = TicketStatusDTO.FAILED;
				finishedTime = Instant.now();
			}
		}
		operation = null;
		runningThread = null;
		resultSignal.countDown();
	}

	@Override
	public T getResult() throws InterruptedException {
		resultSignal.await();
		return result;
	}

	@Override
	public void waitForComplete() throws InterruptedException {
		resultSignal.await();
	}

	public Instant getFinishedTime() {
		synchronized (stateAndTimeLock) {
			return finishedTime;
		}
	}

	@Override
	public TicketStatusDTO cancel(boolean mayInterruptIfRunning) {
		LOGGER.trace("Cancel Ticket {}.", id);
		if (mayInterruptIfRunning && runningThread != null) {
			runningThread.interrupt();
		}
		operation = null;
		result = null;
		resultSignal.countDown();
		synchronized (stateAndTimeLock) {
			if (state == TicketStatusDTO.IN_PROGRESS) {
				state = CANCELED;
			}
			return state;
		}
	}

	@Override
	public byte getPriority() {
		return priority;
	}

	@Override
	public void clean() {
		finishedTime = null;
		operation = null;
		result = null;
		exception = null;
		state = null;
		runningThread = null;
	}

	@Override
	public int compareTo(Ticket<T> other) {
		if (priority == other.priority) {
			return seq < other.seq ? -1 : 1;
		}
		return priority < other.priority ? -1 : 1;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Ticket<?> ticket = (Ticket<?>) o;
		return Objects.equals(id, ticket.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
