package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.util.WorkersConfiguration;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.FeatureDTO;
import at.enfilo.def.transfer.dto.NodeInfoDTO;
import at.enfilo.def.transfer.dto.NodeType;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class WorkerController extends NodeController<IWorkerServiceClient, WorkerServiceClientFactory> {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(WorkerController.class);

	private static WorkerController instance;

	private final Map<String, Set<String>> workerTaskAssignment;
	private final Object assignmentLock;
	private final WorkersConfiguration workersConfiguration;

	private String storeRoutineId;


	public static WorkerController getInstance() {
		if (instance == null) {
			instance = new WorkerController();
		}
		return instance;
	}

	private WorkerController() {
		this(
			new WorkerServiceClientFactory(),
			new LinkedList<>(),
			new HashMap<>(),
			new HashMap<>(),
			null, // null means a TimeoutMap will be created.
			new HashMap<>(),
			Cluster.getInstance().getConfiguration().getWorkersConfiguration(),
			new HashMap<>()
		);
	}

	/**
	 * Internal or Constructor for UnitTests only!
	 *
	 * @param workerServiceClientFactory
	 * @param workers
	 * @param  workerInstanceMap
	 * @param workerConnectionMap
	 * @param workerInfoMap
	 * @param workersConfiguration
	 */
	WorkerController(
			WorkerServiceClientFactory workerServiceClientFactory,
			List<String> workers,
			Map<String, String> workerInstanceMap,
			Map<String, IWorkerServiceClient> workerConnectionMap,
			Map<String, NodeInfoDTO> workerInfoMap,
			Map<String, List<FeatureDTO>> workerFeatureMap,
			WorkersConfiguration workersConfiguration,
			Map<String, Set<String>> workerTaskAssignment
	) {
		super(
				NodeType.WORKER,
				workerServiceClientFactory,
				workers,  workerInstanceMap,
				workerConnectionMap,
				workerInfoMap,
				workerFeatureMap,
				workersConfiguration
		);
		this.assignmentLock = new Object();
		this.workerTaskAssignment = workerTaskAssignment;
		this.workersConfiguration = workersConfiguration;
		this.storeRoutineId = workersConfiguration.getStoreRoutineId();
	}

	/**
	 * Notification that a node is "down".
	 * This notification is triggered by timeout map, if a node does not send an update periodically.
	 *
	 * @param nId - Node id
	 * @param nodeInfo
	 */
	@Override
	protected void notifyNodeDown(String nId, NodeInfoDTO nodeInfo) {
		LOGGER.info("Notification: Node ({}) down. Remove Worker and re-schedule Tasks.", nId);
		try {
			removeNode(nId);
		} catch (UnknownNodeException e) {
			LOGGER.error("Worker {} was already removed.", nId, e);
		}
	}

	/**
	 * Remove a Node from this cluster. Notifies Scheduler to remove the specified Node.
	 * Re-schedule Tasks from "removed" Worker.
	 *
	 * @param nId - Node to remove
	 * @throws UnknownNodeException - if Node id not known
	 */
	@Override
	protected void removeNode(String nId) throws UnknownNodeException {
		// Remove node from internal structures and inform scheduler
		super.removeNode(nId);

		// Re-schedule tasks if needed
		Set<String> taskIds = new HashSet<>();
		synchronized (assignmentLock) {
			if (workerTaskAssignment.containsKey(nId)) {
				taskIds.addAll(workerTaskAssignment.get(nId));
				workerTaskAssignment.remove(nId);
			}
		}
		if (!taskIds.isEmpty()) {
			ClusterExecLogicController.getInstance().reSchedule(taskIds);
		}
	}

	/**
	 * Notification from a worker about new received tasks.
	 *
	 * @param wId - worker id
	 * @param taskIds - list of taks ids
	 * @throws UnknownNodeException
	 */
	void notifyTasksReceived(String wId, List<String> taskIds) throws UnknownNodeException {
		setupWorkerTaskAssignment(wId);
		synchronized (assignmentLock) {
			taskIds.forEach(workerTaskAssignment.get(wId)::add);
		}
	}

	/**
	 * Setup workerTaskAssignment map if needed.
	 *
	 * @param wId - for given worker id
	 * @throws UnknownNodeException
	 */
	private void setupWorkerTaskAssignment(String wId) throws UnknownNodeException {
		synchronized (nodeLock) {
			if (!nodeInfoMap.containsKey(wId)) {
				LOGGER.error(String.format(UNKNOWN_NODE, wId));
				throw new UnknownNodeException(String.format(UNKNOWN_NODE, wId));
			}
		}
		synchronized (assignmentLock) {
			if (!workerTaskAssignment.containsKey(wId)) {
				workerTaskAssignment.put(wId, new HashSet<>());
			}
		}
	}


	public String getStoreRoutineId() {
		return storeRoutineId;
	}

	/**
	 * Set StoreRoutineId.
	 * Updates StoreRoutineId on every registered worker.
	 * @param routineId - new StoreRoutineId
	 */
	void setStoreRoutineId(String routineId) {
		this.storeRoutineId = routineId;
		LOGGER.info("Update StoreRoutine on all Workers to {}.", routineId);
		execOnAllNodes(nodeServiceClient -> {
			try {
				return nodeServiceClient.setStoreRoutine(routineId);
			} catch (ClientCommunicationException e) {
				LOGGER.error("Error while update StoreRoutine on Worker.", e);
				return null;
			}
		});
	}

	/**
	 * Notification from a worker about new task state
	 *
	 * @param wId - worker id
	 * @param taskIds - list of taks ids
	 * @param newState
	 * @throws UnknownNodeException
	 */
	void notifyTasksNewState(String wId, List<String> taskIds, ExecutionState newState) throws UnknownNodeException {
		LOGGER.debug("Notify Tasks have new state {}. Source Worker with id {}.", newState, wId);
		switch (newState) {
			case SUCCESS:
			case FAILED:
				setupWorkerTaskAssignment(wId);
				synchronized (assignmentLock) {
					taskIds.forEach(workerTaskAssignment.get(wId)::remove);
				}
				break;

			case SCHEDULED:
			case RUN:
			default:
				// Ignoring this states
				break;
		}
	}

	/**
	 * Abort the given task.
	 * @param tId - task id to abort
	 */
	void abortTask(String tId) {
		String wId = null;
		synchronized (assignmentLock) {
			for (Map.Entry<String, Set<String>> e : workerTaskAssignment.entrySet()) {
				if (e.getValue().contains(tId)) {
					wId = e.getKey();
					break;
				}
			}
		}

		if (wId != null) {
			synchronized (nodeLock) {
				try {
					Future<Void> futureAbortTask = nodeConnectionMap.get(wId).abortTask(tId);
					futureAbortTask.get(); // wait for ticket to finish
					LOGGER.info(DEFLoggerFactory.createTaskContext(tId), "Aborted Task on Worker {}.", wId);
				} catch (ClientCommunicationException | ExecutionException | InterruptedException e) {
					LOGGER.error(DEFLoggerFactory.createTaskContext(tId), "Error while send abort Task to Worker {}.", wId, e);
				}
			}
		}
	}
}
