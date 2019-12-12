package at.enfilo.def.node.routine.exec;

import at.enfilo.def.common.util.DaemonThreadFactory;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class SequenceProcessWatcher {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(SequenceProcessWatcher.class);
	private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(r -> DaemonThreadFactory.newDaemonThread(r, "RoutineProcessWatcher"));

	private class RoutineProcessWatcher implements Callable<Integer> {
		private final RoutineProcess routineProcess;
		private final SequenceProcessWatcher sequenceProcessWatcher;

		private RoutineProcessWatcher(RoutineProcess routineProcess, SequenceProcessWatcher sequenceProcessWatcher) {
			this.routineProcess = routineProcess;
			this.sequenceProcessWatcher = sequenceProcessWatcher;
		}

		@Override
		public Integer call() throws Exception {
			try {
				int exitValue = routineProcess.getProcess().waitFor();
				if (exitValue > 0) {
					LOGGER.error("RoutineProcess return with error: Routine {} ({}), exit value {}.",
							routineProcess.getSequenceStep().getRoutineId(),
							routineProcess.getSequenceStep().getRoutineType(),
							exitValue
					);
					success = false;
					sequenceProcessWatcher.notifyRoutineProcessError();
				} else {
					LOGGER.debug("RoutineProcess return with success: Routine {} ({}).",
							routineProcess.getSequenceStep().getRoutineId(),
							routineProcess.getSequenceStep().getRoutineType()
					);
				}
				return exitValue;

			} catch (InterruptedException | CancellationException e) {
				LOGGER.debug("Interrupted while waiting for a Routine Process {} ({}): {}.",
						routineProcess.getSequenceStep().getRoutineId(),
						routineProcess.getSequenceStep().getRoutineType(),
						routineProcess.getProcessBuilder().command()
				);
				throw e;
			}
		}
	}

	private final List<RoutineProcess> routineProcesses;
	private final List<Future<Integer>> routineProcessWatchers;
	private boolean success;
	private boolean cancelled;


	public SequenceProcessWatcher(List<RoutineProcess> routineProcesses) {
		this.routineProcesses = routineProcesses;
		this.routineProcessWatchers = new LinkedList<>();
	}

	public boolean waitForAllProcesses() {
		success = true;
		for (RoutineProcess rp : routineProcesses) {
			Future<Integer> f = THREAD_POOL.submit(new RoutineProcessWatcher(rp, this));
			routineProcessWatchers.add(f);
		}
		// Wait for all processes
		for (Future<Integer> f : routineProcessWatchers) {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				LOGGER.debug("Routine Process aborted/interrupted while waiting for all Routine Processes: {}.", e.getMessage());
			}
		}
		return success;
	}

	private synchronized void notifyRoutineProcessError() {
		if (!cancelled) {
			LOGGER.error("At least one Routine exited with error, kill all other routine processes.");
			cancel();
		}
	}


	public synchronized void cancel() {
		if (!cancelled) {
			cancelled = true;
			routineProcesses.parallelStream()
					.map(RoutineProcess::getProcess)
					.filter(Process::isAlive)
					.forEach(Process::destroy);
		}
	}
}
