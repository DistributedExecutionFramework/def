package at.enfilo.def.worker.api;

public class WorkerCommunicationException extends Exception {
	public WorkerCommunicationException(String message) {
		super(message);
	}

	public WorkerCommunicationException(String message, Throwable cause) {
		super(message, cause);
	}

	public WorkerCommunicationException(Throwable cause) {
		super(cause);
	}
}
