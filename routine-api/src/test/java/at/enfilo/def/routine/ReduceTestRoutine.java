package at.enfilo.def.routine;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.impl.Tuple;
import at.enfilo.def.datatype.DEFInteger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReduceTestRoutine extends ReduceRoutine<DEFInteger> {

	private final Map<String, DEFInteger> results;

	public ReduceTestRoutine() {
		super(DEFInteger.class);
		results = new HashMap<>();
	}

	@Override
	protected void reduceValue(String key, DEFInteger value) {
		if (results.containsKey(key)) {
			int newValue = results.get(key).getValue() + value.getValue();
			results.get(key).setValue(newValue);
		} else {
			results.put(key, value);
		}
	}

	@Override
	protected List<ITuple<String, DEFInteger>> finalizeReduce() {
		List<ITuple<String, DEFInteger>> rv = new LinkedList<>();
		results.entrySet().stream().forEach(
				entry -> rv.add(new Tuple<>(entry.getKey(), entry.getValue()))
		);
		return rv;
	}
}
