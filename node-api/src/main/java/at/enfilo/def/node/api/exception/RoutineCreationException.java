package at.enfilo.def.node.api.exception;

public class RoutineCreationException extends Exception {

	public RoutineCreationException(String msg) {
		super(msg);
	}

	public RoutineCreationException(Throwable t) {
		super(t);
	}

	public RoutineCreationException(String msg, Throwable t) {
		super(msg, t);
	}
}
