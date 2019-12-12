package at.enfilo.def.demo;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.impl.Tuple;
import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.routine.MapRoutine;

import java.util.LinkedList;
import java.util.List;

public class DefaultDoubleIntegerMapper extends MapRoutine<DEFDouble, DEFInteger> {

	public DefaultDoubleIntegerMapper() {
		super(DEFDouble.class);
	}

	@Override
	protected List<ITuple<String, DEFInteger>> map(DEFDouble toMap) {
		List<ITuple<String, DEFInteger>> mapping = new LinkedList<>();
		DEFInteger mappedValue = new DEFInteger(Double.valueOf(toMap.getValue()).intValue());
		mapping.add(new Tuple<>(DEFAULT_KEY, mappedValue));
		return mapping;
	}
}
