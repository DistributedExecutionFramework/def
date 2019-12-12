package at.enfilo.def.node.routine.exec;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.util.DaemonThreadFactory;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.exception.RoutineCreationException;
import at.enfilo.def.node.api.exception.RoutineExecutionException;
import at.enfilo.def.node.api.exception.QueueElementExecutionException;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.routine.factory.NamedPipeFactory;
import at.enfilo.def.routine.util.Pipe;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SequenceStepsExecutor {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(SequenceStepsExecutor.class);
	private static final int COMMUNICATOR_JOIN_TIMEOUT = 1000;
	private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(r -> DaemonThreadFactory.newDaemonThread(r, "RoutinesCommunicator"));

	private final String id;
	private final RoutinesCommunicator communicator;
	private final NodeConfiguration configuration;
	private final RoutineProcessBuilderFactory routineProcessBuilderFactory;
	private final List<SequenceStep> steps;
	private final List<RoutineProcess> routineProcesses;
	private final Set<ITuple<ContextIndicator, ?>> logContext;
	private SequenceProcessWatcher sequenceProcessWatcher;


	public SequenceStepsExecutor(
			String id,
			List<SequenceStep> steps,
			Map<String, ResourceDTO> initialInParameters,
			boolean waitForParameters,
			NodeConfiguration configuration,
			RoutineProcessBuilderFactory routineProcessBuilderFactory,
			Set<ITuple<ContextIndicator, ?>> logContext
	) {
		this.id = id;
		this.steps = steps;
		this.configuration = configuration;
		this.routineProcessBuilderFactory = routineProcessBuilderFactory;
		this.routineProcesses = new LinkedList<>();
		this.logContext = logContext;
		this.communicator = setupRoutinesCommunicator(initialInParameters, waitForParameters, steps);
	}


	/**
	 * Creates a RoutinesCommunicator instance.
	 * @param steps - Task Sequence Steps.
	 * @return RoutinesCommunicator
	 */
	private RoutinesCommunicator setupRoutinesCommunicator(
			Map<String, ResourceDTO> inParameters,
			boolean waitForParameters,
			List<SequenceStep> steps
	) {
		Pipe outPipe = steps.get(0).getInPipe();
		List<Pipe> ctrlPipes = steps.stream().map(SequenceStep::getCtrlPipe).collect(Collectors.toList());

		return new RoutinesCommunicator(
				inParameters,
				waitForParameters,
				outPipe,
				ctrlPipes,
				logContext
		);
	}

	/**
	 * Create workingDirectory for the given task.
	 *
	 * @return Path to working directory.
	 * @throws QueueElementExecutionException if an error occurs while trying to create a working dir.
	 */
	private Path setupWorkingDirectory() throws QueueElementExecutionException {
		Path workingDir = Paths.get(configuration.getWorkingDir(), id);
		if (!workingDir.toFile().mkdirs()) {
			LOGGER.error(
				logContext,
				"Error while creating task working directory {}",
				workingDir.toAbsolutePath()
			);

			throw new QueueElementExecutionException("Error while creating task working directory");
		}

		LOGGER.debug(
			logContext,
			"Created working directory {}",
			workingDir.toAbsolutePath().toString()
		);

		return workingDir;
	}

	/**
	 * Runs sequence steps. This means start routine processes.
	 * @return results returned by last routine in sequence.
	 * @throws Exception
	 */
	public List<Result> run() throws Exception {
		return run(new LinkedList<>(), new LinkedList<>());
	}

	/**
	 * Runs sequence steps. This means start routine processes.
	 * @param stdOutConsumers - consumers that consumes all from routine process output on stdout.
	 * @param stdErrConsumers - consumers that consumes all from routine process output on stderr.
	 * @return results returned by last routine in sequence.
	 * @throws Exception
	 */
	public List<Result> run(List<Consumer<String>> stdOutConsumers, List<Consumer<String>> stdErrConsumers) throws Exception {
		List<Result> results = null;


		// Setup working directory (logs, pipes, etc.)
		Path taskWorkingDir = setupWorkingDirectory();

		try {
			// Create all pipes
			LOGGER.debug(logContext, "Creating all needed NamedPipes");
			createAllNamedPipes(steps);

			// Start Communicator
			Future fCommunicator = THREAD_POOL.submit(communicator);

			// Start all Routines (Processes)
			LOGGER.debug(logContext, "Starting all routine processes");
			List<Future> stdOutErrStreamsHandlers = new LinkedList<>();
			for (SequenceStep step : steps) {
				ProcessBuilder processBuilder = routineProcessBuilderFactory.build(taskWorkingDir, step);
				LOGGER.debug(logContext, "Start Process for Routine {}", step.getRoutineId());
				LOGGER.debug(logContext, "Command: {}", String.join(" ", processBuilder.command()));
				// Append loggers to consumers
				//stdOutConsumers.add(this::infoLog);
				//stdErrConsumers.add(this::errorLog);
				// Start routine process
				Process process = processBuilder.start();
				routineProcesses.add(new RoutineProcess(process, processBuilder, step));
				Future stdOutStreamHandler = THREAD_POOL.submit(() -> consumeStream(stdOutConsumers, process.getInputStream()));
				Future stdErrStreamHandler = THREAD_POOL.submit(() -> consumeStream(stdErrConsumers, process.getErrorStream()));
				stdOutErrStreamsHandlers.add(stdOutStreamHandler);
				stdOutErrStreamsHandlers.add(stdErrStreamHandler);
			}

			// Waiting for all routine processes and proof exit codes
			LOGGER.debug(logContext, "Waiting for all routine processes.");
			sequenceProcessWatcher = new SequenceProcessWatcher(routineProcesses);
			boolean success = sequenceProcessWatcher.waitForAllProcesses();
			if (success) {
				LOGGER.debug(logContext, "All routine processes returned with success.");
			} else {
				communicator.shutdown();
				throw new RoutineExecutionException("At least one RoutineProcess exit with error.");
			}

			// Waiting for all stdout and stderr streams
			for (Future f : stdOutErrStreamsHandlers) {
				f.get();
			}

			// Waiting for Communicator
			LOGGER.debug(logContext, "Waiting for RoutineCommunicator thread");
			fCommunicator.get(COMMUNICATOR_JOIN_TIMEOUT, TimeUnit.MILLISECONDS);

			// Create task results (outParameters) as a list of shared resources
			LOGGER.debug(logContext, "Fetch results from Communicator");
			if (communicator.hasResultInfos()) {
				results = communicator.getResults();
			}


		} catch (IOException | RoutineCreationException | InterruptedException | RoutineExecutionException e) {
			LOGGER.error(logContext, "Failed to execute SequenceSteps {}.", id, e);
			throw e;

		} finally { // Cleanup everything.

			// Shutdown communicator
			if (!communicator.isDown()) {
				communicator.shutdown();
			}

			killRunningProcesses(false);
			routineProcesses.clear();

			// Cleanup working dir.
			LOGGER.debug(logContext, "Deleting all created NamedPipes");
			deleteAllNamedPipes(steps);
			taskWorkingDir.toFile().delete();

			if (results == null) {
				results = Collections.emptyList();
			}
		}

		return results;
	}

	private void errorLog(String s) {
		LOGGER.error(logContext, s);
	}

	private void infoLog(String s) {
		LOGGER.info(logContext, s);
	}

	/**
	 * Consumes a input stream to given consumers.
	 * @param consumers
	 * @param is
	 */
	private void consumeStream(List<Consumer<String>> consumers, InputStream is) {
		String buffer;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			while ((buffer = reader.readLine()) != null) {
				for (Consumer<String> consumer : consumers) {
					LOGGER.info("RoutineProcess stdout/stderr: {}", buffer);
					consumer.accept(buffer);
				}
			}
		} catch (IOException e) {
			LOGGER.error(logContext, "Error while consume input stream from process.", e);
		}
	}

	private void killRunningProcesses(boolean forcibly) {
		// Kill all processes, if running
		routineProcesses.stream()
				.filter(rp -> rp.getProcess().isAlive())
				.forEach(rp -> {
						LOGGER.debug(logContext, "Kill process \"{}\".", rp.getProcessBuilder().command());
						if (forcibly) {
							rp.getProcess().destroyForcibly();
						} else {
							rp.getProcess().destroy();
						}
					}
				);
	}

	/**
	 * Cancel Task execution
	 */
	public void cancel() {
		LOGGER.debug(logContext, "Try to cancel task sequence execution.");

		communicator.shutdown();
		LOGGER.debug(logContext, "Routine communicator terminated.");

		while (isRunning()) {
			// First cancel process watcher.
			sequenceProcessWatcher.cancel();

			killRunningProcesses(false);

			try {
				Thread.sleep(1000); // Wait a second and check if kill was successfully
			} catch (InterruptedException e) {
				LOGGER.error("Interrupted while wait for all processes after kill.");
				Thread.currentThread().interrupt();
			}

			killRunningProcesses(true);
		}
		LOGGER.info(logContext, "All routine processes terminated.");
	}

	/**
	 * Create all needed NamedPipes by the task sequence steps.
	 *
	 * @param steps - task sequence steps.
	 */
	private void createAllNamedPipes(List<SequenceStep> steps) {
		steps.forEach((IThrowingConsumer<SequenceStep>) step -> {
			NamedPipeFactory.createPipe(step.getInPipe().resolve());
			NamedPipeFactory.createPipe(step.getCtrlPipe().resolve());
			if (step.isOutPipeSet()) {
				NamedPipeFactory.createPipe(step.getOutPipe().resolve());
			}
		});
	}

	/**
	 * Delete all needed NamedPipes by the task sequence steps.
	 *
	 * @param steps - task sequence steps.
	 */
	private void deleteAllNamedPipes(List<SequenceStep> steps) {
		steps.forEach((IThrowingConsumer<SequenceStep>) step -> {
			NamedPipeFactory.deletePipe(step.getInPipe().resolve());
			NamedPipeFactory.deletePipe(step.getCtrlPipe().resolve());
			if (step.isOutPipeSet()) {
				NamedPipeFactory.deletePipe(step.getOutPipe().resolve());
			}
		});
	}

	/**
	 * Returns true if one of the started routine process is still running.
	 *
	 * @return true if at least one routine process is running, otherwise false.
	 */
	public boolean isRunning() {
		return !routineProcesses.isEmpty() && routineProcesses.stream()
				.map(RoutineProcess::getProcess)
				.anyMatch(Process::isAlive);
	}

	/**
	 * Returns {@link RoutinesCommunicator} for current running steps.
	 * @return
	 */
	public RoutinesCommunicator getCommunicator() {
		return communicator;
	}

	/**
	 *
	 * @return
	 */
	public int getNumberOfSequenceSteps() { return steps.size(); }
}
