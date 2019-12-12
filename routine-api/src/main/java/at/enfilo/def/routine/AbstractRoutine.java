package at.enfilo.def.routine;

import at.enfilo.def.routine.api.Command;
import at.enfilo.def.routine.api.Order;
import at.enfilo.def.routine.util.DataReader;
import at.enfilo.def.routine.util.DataWriter;
import org.apache.thrift.TException;

import java.io.File;
import java.io.IOException;

public abstract class AbstractRoutine implements Runnable {
	private String routineName;
	private String inPipeName;
	private String outPipeName;
	private String ctrlPipeName;

	protected DataReader in;
	protected DataWriter out;
	protected DataWriter ctrl;


	/**
	 * Create an AbstractRoutine with default IO: in pipe and out pipe
	 * @param routineName
	 * @param inPipeName
	 * @param outPipeName
	 * @return an routine instance
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	protected static AbstractRoutine createRoutine(
			String routineName,
			String inPipeName,
			String outPipeName,
			String ctrlPipeName)
	throws ClassNotFoundException, IllegalAccessException, InstantiationException {

		// Instantiate routine
		AbstractRoutine routine = AbstractRoutine.class.cast(Class.forName(routineName).newInstance());
		routine.routineName = routineName;
		routine.inPipeName = "null".equalsIgnoreCase(inPipeName) ? null : inPipeName;
		routine.outPipeName = "null".equalsIgnoreCase(outPipeName) ? null : outPipeName;
		routine.ctrlPipeName = "null".equalsIgnoreCase(ctrlPipeName) ? null: ctrlPipeName;

		return routine;
	}


	@Override
	public void run() {
		try {
			// Setup routine IO
			setupIO(inPipeName, outPipeName, ctrlPipeName);
			log(LogLevel.DEBUG, String.format("Routine %s: IO setup done, run", routineName));

			runRoutine();

			// Send 'done' to RoutinesCommunicator (Worker)
			Order done = new Order(Command.ROUTINE_DONE, "");
			log(LogLevel.DEBUG, "Send ROUTINE_DONE to RoutinesCommunicator and shutdown.");
			ctrl.store(done);

			// Shutdown
			shutdownIO();

		} catch (Exception e) {
			log(LogLevel.ERROR, String.format("Error while running routine %s: %s", routineName, e.getMessage()));
			throw new RoutineException(e);
		}
	}


	/**
	 * Routine body.
	 */
	protected abstract void runRoutine();

	/**
	 * Shutdown IO (pipes).
	 *
	 * @throws IOException
	 */
	protected void shutdownIO() throws IOException {
		// Close all pipes
		if (in != null) {
			in.close();
		}
		if (out != null) {
			out.close();
		}
		if (ctrl != null) {
			ctrl.close();
		}
	}


	/**
	 * Setup communication pipes.
	 * @param inPipe - data input
	 * @param outPipe - data output
	 * @throws IOException
	 */
	protected void setupIO(String inPipe, String outPipe, String ctrlPipe) throws IOException {
		if (in == null) {
			in = inPipe != null ? new DataReader(new File(inPipe)) : null;
		}
		if (out == null) {
			out = outPipe != null ? new DataWriter(new File(outPipe)) : null;
		}
		if (ctrl == null) {
			ctrl = ctrlPipe != null ? new DataWriter(new File(ctrlPipe)) : null;
		}
		log(LogLevel.DEBUG, String.format("inPipe=%s, outPipe=%s, ctrlPipe=%s", inPipe, outPipe, ctrlPipe));
	}


	/**
	 * Logs a message.
	 * @param level - log level
	 * @param msg - message to log
	 */
	protected void log(LogLevel level, String msg) {
		Command cmd;
		switch (level) {
			case ERROR:
				cmd = Command.LOG_ERROR;
				break;
			case DEBUG:
				cmd = Command.LOG_DEBUG;
				break;
			case INFO:
			default:
				cmd = Command.LOG_INFO;
				break;
		}
		try {
			ctrl.store(new Order(cmd, msg));
		} catch (NullPointerException | TException e) {
			String outMsg = String.format("%s %s (Could not send log through ctrl-pipe: %s)", level, msg, e.getMessage());
			switch (level) {
				case ERROR:
					System.err.println(outMsg);
					break;
				case DEBUG:
				case INFO:
				default:
					System.out.println(outMsg);
					break;
			}
		}
	}

	public String getRoutineName() {
		return routineName;
	}

	public String getInPipeName() {
		return inPipeName;
	}

	public String getOutPipeName() {
		return outPipeName;
	}

	public String getCtrlPipeName() {
		return ctrlPipeName;
	}
}
