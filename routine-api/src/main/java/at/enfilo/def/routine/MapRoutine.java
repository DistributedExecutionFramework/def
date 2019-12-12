package at.enfilo.def.routine;

import at.enfilo.def.common.api.ITuple;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;

import java.util.List;


/**
 * Baseclass for MapRoutine
 *
 * @param <T> - Input Type
 * @param <V> - Output Type - value
 */
public abstract class MapRoutine<T extends TBase, V extends TBase> extends AbstractRoutine {

	public static final String DEFAULT_KEY = "DEFAULT";

	private final Class<T> inputType;


	/**
	 * Constructor.
	 * @param inputType
	 */
	protected MapRoutine(Class<T> inputType) {
		this.inputType = inputType;
	}

	private T instance() throws IllegalAccessException, InstantiationException {
		return inputType.newInstance();
	}


	@Override
	public void runRoutine() {
		TSerializer serializer = new TSerializer();

		try {
			log(LogLevel.DEBUG, "Try to receive result from ObjectiveRoutine");
			// Receive object from inPipe
			in.readInt();
			T t = in.read(instance());
			log(LogLevel.DEBUG, String.format("Received from ObjectiveRoutine: %s", t.toString()));

			// Map function
			List<ITuple<String, V>> tuples = map(t);
			log(LogLevel.DEBUG, String.format("Mapping done, created %d tuples", tuples.size()));

			// Tell Receiver the number of tuples
			log(LogLevel.DEBUG, "Storing number of tuples");
			out.store(tuples.size());

			// Store tuples to outPipe
			int i = 1;
			for (ITuple<String, V> tuple : tuples) {
				// Write key
				log(LogLevel.DEBUG, String.format("Write key %d: %s", i, tuple.getKey()));
				out.store(tuple.getKey());
				// Write value (size & data)
				log(LogLevel.DEBUG, String.format("Write value %d: %s", i, tuple.getValue().toString()));
				byte[] value = serializer.serialize(tuple.getValue());
				out.store(value.length); // size
				out.store(value);
				i++;
			}

		} catch (TException | InstantiationException | IllegalAccessException e) {
			log(LogLevel.ERROR, String.format("Error while running map routine: %s", e.getMessage()));
			throw new RoutineException(e);
		}
	}


	/**
	 * Map routine. Map a given T to a Key/Value pair list.
	 *
	 * @param toMap - input
	 * @return key value tuple
	 */
	protected abstract List<ITuple<String, V>> map(T toMap);


	public static void main(String[] args) {
		if (args.length != 4) {
			String errMsg = "4 Arguments needed: <routineName> <inPipe> <outPipe> <ctrlPipe>";
			throw new RoutineException(errMsg);

		} else {
			try {
				AbstractRoutine routine = createRoutine(args[0], args[1], args[2], args[3]);
				routine.run();

			} catch (Exception e) {
				System.err.println(String.format("%s - Error while running MapRoutine: %s", LogLevel.ERROR, e.getMessage()));
				throw new RoutineException(e);
			}
		}
	}

}
