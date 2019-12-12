package at.enfilo.def.node.api.exception;

public class QueueElementExecutionException extends Exception {

	public QueueElementExecutionException(String msg) {
		super(msg);
	}

	public QueueElementExecutionException(Throwable t) {
		super(t);
	}

	public QueueElementExecutionException(String msg, Throwable t) {
		super(msg, t);
	}
}
