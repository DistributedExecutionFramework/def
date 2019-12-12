package at.enfilo.def.node.api;

public class TaskExecutionException extends Exception {

	public TaskExecutionException(String msg) {
		super(msg);
	}

	public TaskExecutionException(Throwable t) {
		super(t);
	}

	public TaskExecutionException(String msg, Throwable t) {
		super(msg, t);
	}
}
