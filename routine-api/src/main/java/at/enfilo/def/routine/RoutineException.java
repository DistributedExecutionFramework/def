package at.enfilo.def.routine;

public class RoutineException extends RuntimeException {
	public RoutineException() {
	}

	public RoutineException(String message) {
		super(message);
	}

	public RoutineException(String message, Throwable cause) {
		super(message, cause);
	}

	public RoutineException(Throwable cause) {
		super(cause);
	}
}
