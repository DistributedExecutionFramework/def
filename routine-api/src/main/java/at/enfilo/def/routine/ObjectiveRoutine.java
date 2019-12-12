package at.enfilo.def.routine;

import at.enfilo.def.routine.api.Command;
import at.enfilo.def.routine.api.Order;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;

import java.io.IOException;

/**
 * ObjectiveRoutine base.
 *
 * Provides an API for library development.
 *
 * To implement: run()
 * Accessing input paramenters: getInParameter()
 * Store result: setResult()
 *
 * @param <R> Result Type
 */
public abstract class ObjectiveRoutine<R extends TBase> extends AbstractRoutine {
	private final TSerializer serializer = new TSerializer();

	protected ObjectiveRoutine() {
	}

	/**
	 * Storing result of library routine.
	 *
	 * @param result - object to store
	 * @throws IOException
	 */
	private void setResult(R result) throws TException {
		log(LogLevel.DEBUG, String.format("Storing Result %s", result.toString()));
		byte[] data = serializer.serialize(result);
		out.store(data.length);
		out.store(data);
	}

	/**
	 * Fetch input parameter by given index.
	 *
	 * @param cls - class of in parameter
	 * @param name - name of parameter
	 * @param <T> - in parameter type
	 * @return - in parameter data
	 * @throws AccessParameterException
	 */
	public <T extends TBase> T getParameter(String name, Class<T> cls) throws AccessParameterException {
		// Request Parameter from WorkerService
		log(LogLevel.DEBUG, String.format("Requesting Parameter %s", name));
		Order getParam = new Order(Command.GET_PARAMETER, name);
		try {
			ctrl.store(getParam);
			T param = in.read(cls.newInstance());
			log(LogLevel.DEBUG, String.format("Received: %s", param));
			return param;

		} catch (IllegalAccessException | InstantiationException | TException e) {
			throw new AccessParameterException(e);
		}
	}


	@Override
	public void runRoutine() {
		// Start routine and store result
		try {
			R result = routine();
			setResult(result);

		} catch (TException e) {
			throw new RoutineException(e);
		}
	}

	/**
	 * ObjectiveRoutine.
	 * Real implementation.
	 *
	 * @return - return parameter
	 * @throws RoutineException
	 */
	protected abstract R routine() throws RoutineException;

	/**
	 * Entry point for ObjectiveRoutine.
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 4) {
			String errMsg = "4 Arguments needed: <routineName> <inPipe> <outPipe> <ctrlPipe>";
			throw new RoutineException(errMsg);

		} else {

			try {
				// Instantiate routine and run
				AbstractRoutine routine = createRoutine(args[0], args[1], args[2], args[3]);
				routine.run();

			} catch (Exception e) {
				System.err.println(String.format("%s - Error while running ObjectiveRoutine: %s", LogLevel.ERROR, e.getMessage()));
				throw new RoutineException(e);
			}
		}
	}
}
