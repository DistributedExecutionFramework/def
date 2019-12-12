package at.enfilo.def.scheduler.reducer.api;

public class ReduceOperationException extends Exception {
	public ReduceOperationException(String message) {
		super(message);
	}

	public ReduceOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReduceOperationException(Exception e) {
		super(e);
	}
}
