package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.api.NodeCreationException;
import at.enfilo.def.cluster.api.NodeExecutionException;
import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.util.configuration.WorkersConfiguration;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.scheduler.worker.api.IWorkerSchedulerServiceClient;
import at.enfilo.def.transfer.dto.*;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class WorkerController extends NodeController<IWorkerServiceClient, WorkerServiceClientFactory> {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(WorkerController.class);

	private static WorkerController instance;

	private final Map<String, Set<String>> workerTaskAssignment;
	private final IWorkerSchedulerServiceClient workerSchedulerServiceClient;

	private static final Object INSTANCE_LOCK = new Object();
	private final Object assignmentLock;

	public static WorkerController getInstance() {
		synchronized (INSTANCE_LOCK) {
			if (instance == null) {
				instance = new WorkerController();
			}
			return instance;
		}
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
				workers,
				workerInstanceMap,
				workerConnectionMap,
				workerInfoMap,
				workerFeatureMap,
				workersConfiguration,
				workersConfiguration.getStoreRoutineId()
		);
		this.assignmentLock = new Object();
		this.workerTaskAssignment = workerTaskAssignment;
		this.workerSchedulerServiceClient = ClusterResource.getInstance().getWorkerSchedulerServiceClient();
	}

	public String addWorker(ServiceEndpointDTO serviceEndpoint) throws NodeCreationException {
		return super.addNode(serviceEndpoint);
	}

	public String getStoreRoutineId() {
		return super.getStoreRoutineId();
	}

	public void distributeSharedResource(ResourceDTO sharedResource) {
		super.distributeSharedResource(sharedResource);
	}

	public void removeSharedResources(List<String> sharedResources) {
		super.removeSharedResources(sharedResources);
	}

	public void setStoreRoutineId(String storeRoutineId) {
		super.setStoreRoutineId(storeRoutineId);
	}

	/**
	 * Remove a Node from this cluster. Notifies Scheduler to remove the specified Node.
	 * Re-schedule Tasks from "removed" Worker.
	 *
	 * @param nId - Node to remove
	 * @throws UnknownNodeException - if Node id not known
	 */
	@Override
	protected void removeNodeAssignments(String nId) throws UnknownNodeException {
		// Re-schedule tasks if needed
		LOGGER.debug("Removing node {}.", nId);
		Set<String> taskIds = new HashSet<>();
		synchronized (assignmentLock) {
			if (workerTaskAssignment.containsKey(nId)) {
				taskIds.addAll(workerTaskAssignment.get(nId));
				workerTaskAssignment.remove(nId);
			}
		}
		if (!taskIds.isEmpty()) {
			LOGGER.info("Re-scheduling tasks of worker {}.", nId);
			ClusterExecLogicController.getInstance().reScheduleTasks(taskIds);
		}
	}

	public void removeNode(String nId) throws UnknownNodeException {
		super.removeNode(nId, true);
	}

	/**
	 * Notification from a worker about new received tasks.
	 *
	 * @param wId - worker id
	 * @param taskIds - list of task ids
	 * @throws UnknownNodeException
	 */
	void notifyTasksReceived(String wId, List<String> taskIds) throws UnknownNodeException {
		LOGGER.debug("{} tasks received from worker {}.", taskIds.size(), wId);
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

	/**
	 * Notification from a worker about new task state
	 *
	 * @param wId - worker id
	 * @param taskIds - list of taks ids
	 * @param newState
	 * @throws UnknownNodeException
	 */
	void notifyTasksNewState(String wId, List<String> taskIds, ExecutionState newState) throws UnknownNodeException {
		LOGGER.debug("Notify tasks have new state {} from worker {}.");
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
	public void abortTask(String tId) throws NodeExecutionException {
		LOGGER.debug(DEFLoggerFactory.createTaskContext(tId), "Aborting task on worker.");
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
			try {
				Future<Void> future = workerSchedulerServiceClient.abortTask(wId, tId);
				future.get(); // Wait for done

			} catch (ExecutionException | ClientCommunicationException e) {
				LOGGER.error(DEFLoggerFactory.createTaskContext(tId), "Error while send abort Task to Worker {}.", wId, e);
				throw new NodeExecutionException(e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.error(DEFLoggerFactory.createTaskContext(tId), "Error while send abort Task to Worker {}. Interrupted.", wId, e);
				throw new NodeExecutionException(e);
			}
		}
	}

	public void runTask(TaskDTO task) throws NodeExecutionException {
		if (task != null) {
			LOGGER.debug(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()), "Scheduling task to workers.");
			try {
				Future<Void> future = workerSchedulerServiceClient.scheduleTask(task.getJobId(), task);
				future.get(); // wait for done
			} catch (ExecutionException | ClientCommunicationException e) {
				LOGGER.error(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()), "Error while scheduling task.", e);
				throw new NodeExecutionException(e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.error(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()), "Error while scheduling task. Interrupted.", e);
				throw new NodeExecutionException(e);
			}
		} else {
			String msg = "Cannot run/schedule Task. Task is null.";
			LOGGER.error(msg);
			throw new NodeExecutionException(msg);
		}
	}

	public void addJob(String jId) throws NodeExecutionException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Adding job to workers.");
		try {
			Future<Void> future = workerSchedulerServiceClient.addJob(jId);
			future.get(); // Wait for done.
		} catch (ExecutionException | ClientCommunicationException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while adding a new job.", e);
			throw new NodeExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while adding a new job. Interrupted.", e);
			throw new NodeExecutionException(e);
		}
	}

	public void deleteJob(String jId) throws NodeExecutionException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Removing job from workers.");
		try {
			Future<Void> future = workerSchedulerServiceClient.removeJob(jId);
			future.get();

		} catch (ExecutionException | ClientCommunicationException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while removing job.");
			throw new NodeExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while removing job. Interrupted.");
			throw new NodeExecutionException(e);
		}
	}

	public void pauseQueue(String jId) {
		LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Pause queue on workers.");
		execOnAllNodes(workerServiceClient -> {
			try {
				return workerServiceClient.pauseQueue(jId);
			} catch (ClientCommunicationException e) {
				LOGGER.error("Error while pause queue {} on worker.", jId);
				return null;
			}
		});
	}

	public void markJobAsComplete(String jId) throws NodeExecutionException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Marking job as complete on workers.");
		try {
			Future<Void> future = workerSchedulerServiceClient.markJobAsComplete(jId);
			future.get(); // wait for done
		} catch (ExecutionException | ClientCommunicationException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while marking job as complete.");
			throw new NodeExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while marking job as complete. Interrupted.");
			throw new NodeExecutionException(e);
		}
	}

	public TaskDTO fetchFinishedTask(String wId, String tId) throws UnknownNodeException, NodeExecutionException {
		LOGGER.debug(DEFLoggerFactory.createTaskContext(tId), "Fetching finished task.");
		try {
			return getServiceClient(wId).fetchFinishedTask(tId).get();
		} catch (ClientCommunicationException | InterruptedException | ExecutionException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(tId), "Error while fetching finished task from worker.");
			throw new NodeExecutionException(e);
		}
	}
}
