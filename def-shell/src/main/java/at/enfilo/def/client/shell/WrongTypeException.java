package at.enfilo.def.client.shell;

class WrongTypeException extends RuntimeException {
	public WrongTypeException(String message) {
		super(message);
	}

	public WrongTypeException(String format, Exception e) {
		super(format, e);
	}
}
