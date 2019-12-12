package at.enfilo.def.communication.api.ticket;

import at.enfilo.def.communication.dto.TicketStatusDTO;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface ITicket<T> {
	byte SERVICE_PRIORITY = 0x00;
	byte HIGHER_THAN_NORMAL_PRIORITY = 0x10;
	byte NORMAL_PRIORITY = 0x20;
	byte LOWER_THAN_NORMAL_PRIORITY = 0x30;

	UUID getId();

	Class<T> getResultClass();

	TicketStatusDTO getStatus();

	String getException();

	T getResult() throws InterruptedException, ExecutionException;

	TicketStatusDTO cancel(boolean mayInterruptIfRunning);

	void clean();

	byte getPriority();

	void waitForComplete() throws InterruptedException;
}
