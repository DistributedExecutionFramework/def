package at.enfilo.def.communication.api.ticket;

import at.enfilo.def.communication.dto.TicketStatusDTO;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface ITicket<T> {
	int SERVICE_PRIORITY = -2;
	int NORMAL_PRIORITY = 0;
	int HIGHER_THAN_NORMAL_PRIORITY = -1;
	int LOWER_THAN_NORMAL_PRIORITY = 1;

	UUID getId();

	Class<T> getResultClass();

	TicketStatusDTO getStatus();

	String getException();

	T getResult() throws InterruptedException, ExecutionException;

	TicketStatusDTO cancel(boolean mayInterruptIfRunning);

	void clean();

	int getPriority();

	void waitForComplete() throws InterruptedException;
}
