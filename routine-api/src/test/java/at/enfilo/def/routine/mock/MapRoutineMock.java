package at.enfilo.def.routine.mock;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.routine.MapRoutine;

import java.util.List;

public class MapRoutineMock extends MapRoutine<DEFDouble, DEFDouble> {

	private static MapRoutineMock lastInstance;

	private boolean run = false;

	@Override
	protected List<ITuple<String, DEFDouble>> map(DEFDouble toMap) {
		return null;
	}

	public MapRoutineMock() {
		super(DEFDouble.class);
		lastInstance = this;
	}

	@Override
	public void run() {
		run = true;
	}


	public static MapRoutineMock getLastInstance() {
		return lastInstance;
	}

	public boolean isRun() {
		return run;
	}

}
