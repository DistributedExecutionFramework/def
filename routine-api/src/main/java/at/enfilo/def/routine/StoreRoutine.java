package at.enfilo.def.routine;

import at.enfilo.def.routine.api.Command;
import at.enfilo.def.routine.api.Order;
import at.enfilo.def.routine.api.Result;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public abstract class StoreRoutine extends AbstractRoutine {

	/**
	 * Store key / value pair and returns a ResultInfo object.
	 *
	 * @param key - key
	 * @param data - value
	 * @param tupleSeq - current tuple seq#
	 * @throws IOException
	 */
	protected abstract Result store(String key, byte[] data, int tupleSeq) throws IOException;


	@Override
	public void runRoutine() {
		try {
			List<Result> results = new LinkedList<>();

			log(LogLevel.DEBUG, "Try to receive from PartitionRoutine");
			// fetch nr of tuples
			int tuples = in.readInt();
			log(LogLevel.DEBUG, String.format("%d Tuples to process.", tuples));

			for (int i = 0; i < tuples; i++) {
				// receive key and value from in stream
				String key = in.readString();
				int size = in.readInt();
				byte[] data = in.readBytes(size);

				// store key value pair
				Result result = store(key, data, i);
				results.add(result);
				log(LogLevel.DEBUG, String.format("Stored %d/%d: key %s and %d data bytes.", i + 1, tuples, key, size));
			}

			// send ResultInfos to RoutineCommunicator (Worker)
			log(LogLevel.DEBUG, String.format("Send %d StoreResult object to RoutinesCommunicator (Worker)", tuples));
			Order order = new Order(Command.SEND_RESULT, Integer.toString(tuples));
			ctrl.store(order);
			for (Result result : results) {
				ctrl.store(result);
			}


		} catch (TException | IOException e) {
			log(LogLevel.ERROR, String.format("Error while running StoreRoutine: %s", e.getMessage()));
			throw new RoutineException(e);
		}
	}


	/**
	 * Configure this routine with the given configuration file.
	 *
	 * @param configFile - configuration file
	 */
	protected abstract void configure(String configFile);


	/**
	 * Shutdown storage IO.
	 *
	 * @throws IOException
	 */
	protected abstract void shutdownStorage() throws IOException;


	/**
	 * Setup storage IO.
	 *
	 * @throws IOException
	 */
	protected abstract void setupStorage() throws IOException;


	/**
	 * StoreRoutine main entry paint.
	 * @param args
	 */
	public static void main(String[] args) {
		if ((args.length != 3) && (args.length != 4)) {
			String errMsg = "3/4 Arguments needed: <routineName> <inPipe> <ctrlPipe> [<configFile>]";
			throw new RoutineException(errMsg);

		} else {

			// Parse arguments
			int arg = 0;
			String routineName = args[arg++];
			String inPipeName = args[arg++];
			String ctrlPipeName = args[arg++];
			String configFile = null;
			if (args.length > arg) {
				configFile = args[arg];
			}

			StoreRoutine routine;
			try {
				// Instantiate routine
				routine = StoreRoutine.class.cast(
						createRoutine(routineName, inPipeName, null, ctrlPipeName)
				);

				// Setup program, job and task infos

				if (configFile != null) {
					routine.log(LogLevel.DEBUG, String.format("Configure StoreRoutine %s with %s", routineName, configFile));
					routine.configure(configFile);
				}

				// Setup specific IO
				routine.setupStorage();

				routine.run();

				// Shutdown specific IO
				routine.shutdownStorage();

			} catch (Exception e) {
				System.err.println(String.format("%s - Error while running StoreRoutine: %s", LogLevel.ERROR, e.getMessage()));
				throw new RoutineException(e);
			}
		}
	}

}

