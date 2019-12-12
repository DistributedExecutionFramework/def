package at.enfilo.def.routine.mock;

import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.routine.ObjectiveRoutine;

import java.io.IOException;

public class ObjectiveRoutineMock extends ObjectiveRoutine<DEFDouble> {

	private static ObjectiveRoutineMock lastInstance;

	private boolean run = false;
	private boolean shutdownIO = false;

	public ObjectiveRoutineMock() {
		lastInstance = this;
	}

	@Override
	protected DEFDouble routine() {
		return null;
	}

	@Override
	public void run() {
		run = true;
	}

	@Override
	protected void shutdownIO() throws IOException {
		shutdownIO = true;
	}

	public static ObjectiveRoutineMock getLastInstance() {
		return lastInstance;
	}

	public boolean isRun() {
		return run;
	}

}
