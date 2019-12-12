package at.enfilo.def.scheduler.api;

public class ScheduleReduceException extends Exception {
	public ScheduleReduceException(String message) {
		super(message);
	}

	public ScheduleReduceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScheduleReduceException(Exception e) {
		super(e);
	}
}
