package at.enfilo.def.routine;

public class WrongTypeException extends RoutineException {
	public WrongTypeException() {
	}

	public WrongTypeException(String message) {
		super(message);
	}

	public WrongTypeException(String message, Throwable cause) {
		super(message, cause);
	}
}
