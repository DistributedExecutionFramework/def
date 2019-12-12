package at.enfilo.def.node.api.exception;

import org.apache.thrift.TException;

public class QueueNotExistsException extends TException {

	public QueueNotExistsException() {
	}

	public QueueNotExistsException(String msg) {
		super(msg);
	}

	public QueueNotExistsException(Throwable t) {
		super(t);
	}

	public QueueNotExistsException(String msg, Throwable t) {
		super(msg, t);
	}
}
