package at.enfilo.def.routine;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.impl.Tuple;
import at.enfilo.def.datatype.DEFInteger;

import java.util.LinkedList;
import java.util.List;

public class MapTestRoutine extends MapRoutine<Person, DEFInteger> {

	public MapTestRoutine() {
		super(Person.class);
	}

	@Override
	protected List<ITuple<String, DEFInteger>> map(Person person) {
		List<ITuple<String, DEFInteger>> tuples = new LinkedList<>();
		tuples.add(new Tuple<>(DEFAULT_KEY, new DEFInteger(person.getAge())));
		return tuples;
	}
}
