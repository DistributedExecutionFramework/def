package at.enfilo.def.routine;

import at.enfilo.def.datatype.DEFInteger;
import at.enfilo.def.datatype.DEFString;


/**
 * Simple Test Routine.
 *
 * Construct a person class from given attributes.
 */
public class ObjectiveTestRoutine extends ObjectiveRoutine<Person> {

	@Override
	protected Person routine() throws RoutineException {
		try {
			DEFString firstName = getParameter("firstName", DEFString.class);
			DEFString lastName = getParameter("lastName", DEFString.class);
			DEFInteger age = getParameter("age", DEFInteger.class);

			return new Person(firstName.getValue(), lastName.getValue(), (short) age.getValue());

		} catch (AccessParameterException e) {
			throw new RoutineException(e);
		}
	}
}
