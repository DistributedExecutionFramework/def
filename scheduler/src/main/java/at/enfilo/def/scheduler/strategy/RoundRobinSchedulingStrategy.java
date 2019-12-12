package at.enfilo.def.scheduler.strategy;

import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.scheduler.api.ScheduleTaskException;
import at.enfilo.def.scheduler.util.SchedulerConfiguration;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple round robin scheduling strategy. This strategy takes always the next node in list.
 */
public class RoundRobinSchedulingStrategy extends TaskSchedulingStrategy {

	private static final Logger LOGGER = LoggerFactory.getLogger(RoundRobinSchedulingStrategy.class);
	private int counter = 0;

	public RoundRobinSchedulingStrategy(
		SchedulerConfiguration schedulerConfiguration
	) {
		super(schedulerConfiguration);
	}

	RoundRobinSchedulingStrategy(
		Map<String, IWorkerServiceClient> workers,
		Map<String, Environment> workerEnvironments,
		SchedulerConfiguration schedulerConfiguration,
		WorkerServiceClientFactory workerServiceClientFactory,
		ILibraryServiceClient libraryServiceClient
	) {
		super(
			Collections.synchronizedSet(new HashSet<>()),
			new ConcurrentHashMap<>(),
			workers, workerEnvironments,
			workerServiceClientFactory,
				libraryServiceClient,
			schedulerConfiguration
		);
	}

	@Override
	public void prepareForSchedule(int tasks) {
		// not needed by this scheduler
	}

	@Override
	public String nextWorkerId(List<String> rIds) throws ScheduleTaskException {
		// Fetching registered nodes.
		List<String> nodes = getWorkers(rIds);

		// Next node in round robin style.
		if (nodes == null || nodes.isEmpty()) {
			LOGGER.error("No nodes set, returning null.");
			return null;
		}

        // Required to prevent int overflow.
        counter %= nodes.size();
		return nodes.get(counter++);
	}

	/**
	 * Notify about node state.
	 *
	 * @param nId - node id
	 * @param nodeInfo - information (nr of tasks, load, etc) about node.
	 */
	@Override
	public void notifyNodeInfo(String nId, NodeInfoDTO nodeInfo) {
		// not needed, because RoundRobin is a static scheduler
	}

	/**
	 * Notify that tasks {@code taskIds} reached a new state on node {@code nId}.
	 *
	 * @param wId      - worker id.
	 * @param taskIds  - tasks that were successfully executed (finished) ny node {@code nId}.
	 * @param newState - new state of tasks
	 */
	@Override
	public void notifyTasksNewState(String wId, List<String> taskIds, ExecutionState newState) {

	}

	/**
	 * Returns true if scheduling strategy should/must wait for all tasks.
	 *
	 * @return
	 */
	@Override
	protected boolean waitForAllTasks() {
		return false;
	}
}
