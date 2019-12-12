package at.enfilo.def.scheduler.worker.api;

public class TaskOperationException extends Exception {

	public TaskOperationException(String msg) {
		super(msg);
	}

	public TaskOperationException(Throwable t) {
		super(t);
	}

	public TaskOperationException(String msg, Throwable t) {
		super(msg, t);
	}
}
