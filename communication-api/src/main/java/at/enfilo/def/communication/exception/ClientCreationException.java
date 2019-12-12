package at.enfilo.def.communication.exception;

public class ClientCreationException extends Exception {

	public ClientCreationException(String msg) {
		super(msg);
	}

	public ClientCreationException(Throwable t) {
		super(t);
	}

	public ClientCreationException(String msg, Throwable t) {
		super(msg, t);
	}
}
