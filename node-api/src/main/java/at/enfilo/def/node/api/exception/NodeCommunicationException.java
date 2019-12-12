package at.enfilo.def.node.api.exception;

public class NodeCommunicationException extends Exception {

	public NodeCommunicationException(String msg) {
		super(msg);
	}

	public NodeCommunicationException(Throwable t) {
		super(t);
	}

	public NodeCommunicationException(String msg, Throwable t) {
		super(msg, t);
	}
}
