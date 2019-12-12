package at.enfilo.def.cluster.api;

public class UnknownNodeException extends Exception {
	public UnknownNodeException() {
	}

	public UnknownNodeException(String message) {
		super(message);
	}

	public UnknownNodeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownNodeException(Throwable cause) {
		super(cause);
	}

	public UnknownNodeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
