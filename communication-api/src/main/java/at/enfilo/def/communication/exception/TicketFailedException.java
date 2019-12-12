package at.enfilo.def.communication.exception;

import java.util.concurrent.ExecutionException;

public class TicketFailedException extends ExecutionException {
	public TicketFailedException(String message) {
		super(message);
	}
}
