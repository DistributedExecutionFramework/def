package at.enfilo.def.demo;

import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.routine.AccessParameterException;
import at.enfilo.def.routine.ObjectiveRoutine;
import at.enfilo.def.routine.RoutineException;

public class PICalc extends ObjectiveRoutine<DEFDouble> {

	@Override
	protected DEFDouble routine() throws RoutineException {
		try {
			double start = getParameter("start", DEFDouble.class).getValue();
			double end = getParameter("end", DEFDouble.class).getValue();
			double stepSize = getParameter("stepSize", DEFDouble.class).getValue();

			double sum = 0.0;
			for (double i = start; i < end; i++) {
				double x = (i + 0.5) * stepSize;
				sum += 4.0 / (1.0 + x * x);
			}
			sum *= stepSize;

			System.out.println("Pi part successful calculated: " + sum);

			return new DEFDouble(sum);

		} catch (AccessParameterException e) {
			throw new RoutineException(e);
		}
	}
}
