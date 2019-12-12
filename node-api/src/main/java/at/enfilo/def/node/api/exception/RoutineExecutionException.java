package at.enfilo.def.node.api.exception;

public class RoutineExecutionException extends Exception {

	public RoutineExecutionException() {
	}

	public RoutineExecutionException(String msg) {
		super(msg);
	}

	public RoutineExecutionException(Throwable t) {
		super(t);
	}

	public RoutineExecutionException(String msg, Throwable t) {
		super(msg, t);
	}
}
