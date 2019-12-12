package at.enfilo.def.communication.exception;

public class ServerStartupException extends Exception {
	public ServerStartupException(String message) {
		super(message);
	}

	public ServerStartupException(Throwable t) {
		super(t);
	}
}
