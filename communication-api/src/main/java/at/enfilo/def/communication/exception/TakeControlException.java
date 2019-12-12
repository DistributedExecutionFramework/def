package at.enfilo.def.communication.exception;

public class TakeControlException extends Exception {
	public TakeControlException() {
	}

	public TakeControlException(String message) {
		super(message);
	}

	public TakeControlException(String message, Throwable cause) {
		super(message, cause);
	}

	public TakeControlException(Throwable cause) {
		super(cause);
	}

	public TakeControlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
