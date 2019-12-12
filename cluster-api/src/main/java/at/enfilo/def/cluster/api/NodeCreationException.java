package at.enfilo.def.cluster.api;

public class NodeCreationException extends Exception {
	public NodeCreationException() {
	}

	public NodeCreationException(String message) {
		super(message);
	}

	public NodeCreationException(String message, Throwable cause) {
		super(message, cause);
	}

	public NodeCreationException(Throwable cause) {
		super(cause);
	}

	public NodeCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
