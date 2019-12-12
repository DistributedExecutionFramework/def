package at.enfilo.def.routine.mock;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.routine.ReduceRoutine;

import java.util.List;

public class ReduceRoutineMock extends ReduceRoutine<DEFDouble> {

	private static ReduceRoutineMock lastInstance;

	private boolean run = false;

	@Override
	protected void reduceValue(String key, DEFDouble value) {

	}

	@Override
	protected List<ITuple<String, DEFDouble>> finalizeReduce() {
		return null;
	}

	public ReduceRoutineMock() {
		super(DEFDouble.class);
		lastInstance = this;
	}

	@Override
	public void run() {
		run = true;
	}


	public static ReduceRoutineMock getLastInstance() {
		return lastInstance;
	}

	public boolean isRun() {
		return run;
	}

}
