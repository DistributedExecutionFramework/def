package at.enfilo.def.demo;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.impl.Tuple;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.routine.ReduceRoutine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DoubleSumReducer extends ReduceRoutine<DEFDouble> {
	private final Map<String, DEFDouble> results;

	public DoubleSumReducer() {
		super(DEFDouble.class);
		results = new HashMap<>();
	}

	@Override
	protected void reduceValue(String key, DEFDouble value) {
		if (results.containsKey(key)) {
			double sum = results.get(key).getValue() + value.getValue();
			results.get(key).setValue(sum);
		} else {
			results.put(key, value);
		}
	}

	@Override
	protected List<ITuple<String, DEFDouble>> finalizeReduce() {
		List<ITuple<String, DEFDouble>> rv = new LinkedList<>();
		results.entrySet().forEach(
				entry -> rv.add(new Tuple<>(entry.getKey(), entry.getValue()))
		);
		return rv;
	}
}
