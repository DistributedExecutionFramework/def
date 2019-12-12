package at.enfilo.def.cluster.api;

public class UnknownWorkerException extends Exception {
	public UnknownWorkerException() {
	}

	public UnknownWorkerException(String message) {
		super(message);
	}

	public UnknownWorkerException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownWorkerException(Throwable cause) {
		super(cause);
	}

	public UnknownWorkerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
