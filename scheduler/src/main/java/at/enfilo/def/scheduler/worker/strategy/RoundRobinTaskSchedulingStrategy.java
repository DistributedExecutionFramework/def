package at.enfilo.def.scheduler.worker.strategy;

import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.scheduler.general.impl.RoundRobinScheduler;
import at.enfilo.def.scheduler.general.util.SchedulerConfiguration;
import at.enfilo.def.scheduler.worker.api.TaskOperationException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple round robin scheduling strategy. This strategy takes always the next node in list.
 */
public class RoundRobinTaskSchedulingStrategy extends TaskSchedulingStrategy {

	private final RoundRobinScheduler roundRobinScheduler;

	public RoundRobinTaskSchedulingStrategy(
		SchedulerConfiguration schedulerConfiguration
	) {
		super(schedulerConfiguration);
		this.roundRobinScheduler = new RoundRobinScheduler();
	}

	RoundRobinTaskSchedulingStrategy(
		Map<String, IWorkerServiceClient> workers,
		Map<String, Environment> workerEnvironments,
		SchedulerConfiguration schedulerConfiguration,
		WorkerServiceClientFactory workerServiceClientFactory,
		ILibraryServiceClient libraryServiceClient
	) {
		super(
			Collections.synchronizedSet(new HashSet<>()),
			new ConcurrentHashMap<>(),
			workers,
			workerEnvironments,
			workerServiceClientFactory,
			libraryServiceClient,
			schedulerConfiguration
		);
		this.roundRobinScheduler = new RoundRobinScheduler();
	}

	@Override
	public void prepareForSchedule(int tasks) {
		// not needed by this scheduler
	}

	@Override
	public String nextWorkerId(List<String> rIds) throws TaskOperationException {
		return roundRobinScheduler.nextId(getWorkers(rIds));
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
