package at.enfilo.def.scheduler.api;

public class ScheduleTaskException extends Exception {

	public ScheduleTaskException(String msg) {
		super(msg);
	}

	public ScheduleTaskException(Throwable t) {
		super(t);
	}

	public ScheduleTaskException(String msg, Throwable t) {
		super(msg, t);
	}
}
