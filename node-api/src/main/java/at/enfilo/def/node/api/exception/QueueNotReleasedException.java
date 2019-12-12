package at.enfilo.def.node.api.exception;

public class QueueNotReleasedException extends Exception {

	/**
	 * Constructs a new exception with {@code null} as its detail message.
	 * The cause is not initialized, and may subsequently be initialized by a
	 * call to {@link #initCause}.
	 */
	public QueueNotReleasedException() {
	}

	/**
	 * Constructs a new exception with the specified detail message.  The
	 * cause is not initialized, and may subsequently be initialized by
	 * a call to {@link #initCause}.
	 *
	 * @param msg the detail message. The detail message is saved for
	 *                later retrieval by the {@link #getMessage()} method.
	 */
	public QueueNotReleasedException(String msg) {
		super(msg);
	}

	public QueueNotReleasedException(Throwable t) {
		super(t);
	}

	public QueueNotReleasedException(String msg, Throwable t) {
		super(msg, t);
	}
}
