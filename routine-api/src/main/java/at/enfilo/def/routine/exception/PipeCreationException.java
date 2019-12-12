package at.enfilo.def.routine.exception;

public class PipeCreationException extends Exception {

	public PipeCreationException(String msg) {
		super(msg);
	}

	public PipeCreationException(Throwable t) {
		super(t);
	}

	public PipeCreationException(String msg, Throwable t) {
		super(msg, t);
	}
}
