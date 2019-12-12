package at.enfilo.def.communication.exception;

public class ServerShutdownException extends Exception {
	public ServerShutdownException(String message) {
		super(message);
	}

	public ServerShutdownException(Throwable throwable) {
		super(throwable);
	}
}
