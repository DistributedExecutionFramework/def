package at.enfilo.def.routine;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.routine.api.Command;
import at.enfilo.def.routine.api.Order;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;

import java.util.List;

/**
 * ReduceRoutine base.
 *
 * Provides an API for library development.
 *
 * @param <R> Result Type
 */
public abstract class ReduceRoutine<R extends TBase> extends AbstractRoutine {
	private static final String REDUCE_KEY = "REDUCE";

	private final TSerializer serializer = new TSerializer();
	private final Class<R> type;

	protected ReduceRoutine(Class<R> type) {
		this.type = type;
	}

	protected abstract void reduceValue(String key, R value);

	protected abstract List<ITuple<String, R>> finalizeReduce();

	@Override
	protected void runRoutine() {
		int index = 0;
		try {
			boolean hasMore = true;
			while (hasMore) {
				String key = fetchReduceKey(index);
				if (key.equals(REDUCE_KEY)) {
					hasMore = false;
					continue;
				}
				R value = fetchReduceValue(index);
				reduceValue(key, value);
				index++;
			}

			List<ITuple<String, R>> reducedResults = finalizeReduce();

			// Tell Receiver the number of tuples
			log(LogLevel.DEBUG, String.format("Storing number of tuples (reduced results): %d.", reducedResults.size()));
			out.store(reducedResults.size());

			// Store tuples to outPipe
			int i = 1;
			for (ITuple<String, R> tuple : reducedResults) {
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

		} catch (AccessParameterException | TException e) {
			log(LogLevel.ERROR, String.format("Error while run reduce routine. Message: %s.", e.getMessage()));
		}
	}

	private R fetchReduceValue(int index) throws AccessParameterException {
		log(LogLevel.DEBUG, String.format("Requesting reduce value #%d", index));
		Order getParam = new Order(Command.GET_PARAMETER, Integer.toString(index));
		try {
			ctrl.store(getParam);
			R value = in.read(type.newInstance());
			log(LogLevel.DEBUG, String.format("Received reduce value: %s", value));
			return value;

		} catch (IllegalAccessException | InstantiationException | TException e) {
			throw new AccessParameterException(e);
		}
	}

	private String fetchReduceKey(int index) throws AccessParameterException {
		log(LogLevel.DEBUG, String.format("Requesting reduce key #%d", index));
		Order getKey = new Order(Command.GET_PARAMETER_KEY, Integer.toString(index));
		try {
			ctrl.store(getKey);
			String key = in.readString();
			log(LogLevel.DEBUG, String.format("Received reduce key: %s", key));
			return key;

		} catch (TException e) {
			throw new AccessParameterException(e);
		}
	}

	/**
	 * Entry point for ReduceRoutine.
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
				System.err.println(String.format("%s - Error while running ReduceRoutine: %s", LogLevel.ERROR, e.getMessage()));
				throw new RoutineException(e);
			}
		}
	}
}
