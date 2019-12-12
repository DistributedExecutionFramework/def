package at.enfilo.def.scheduler.strategy;

import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.NodeCommunicationException;
import at.enfilo.def.scheduler.api.ScheduleTaskException;
import at.enfilo.def.scheduler.api.strategy.ITaskSchedulingStrategy;
import at.enfilo.def.scheduler.util.SchedulerConfiguration;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Abstract base class for every scheduler implementation.
 *
 */
public abstract class TaskSchedulingStrategy extends SchedulingStrategy<IWorkerServiceClient, WorkerServiceClientFactory>
implements ITaskSchedulingStrategy {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(TaskSchedulingStrategy.class);

	private final Set<String> jobs;
	private final Map<String, List<TaskDTO>> taskBuffer; // <jId, List<TaskDTO>>

	private boolean waitForAllTasks;

	/**
	 * Constructor for implementation.
	 *
	 */
	protected TaskSchedulingStrategy(SchedulerConfiguration schedulerConfiguration) {
		this(
			Collections.synchronizedSet(new HashSet<>()),
			new ConcurrentHashMap<>(),
			new ConcurrentHashMap<>(),
			new ConcurrentHashMap<>(),
			new WorkerServiceClientFactory(),
			null,
			schedulerConfiguration
		);
	}

	/**
	 * Constructor for unit test - concrete Class TestScheduler.
	 *
	 * @param jobs - internal job list.
	 * @param taskBuffer - buffer for tasks.
	 * @param workers - internal worker map.
	 */
	protected TaskSchedulingStrategy(
		Set<String> jobs,
		Map<String, List<TaskDTO>> taskBuffer,
		Map<String, IWorkerServiceClient> workers,
		Map<String, Environment> workerEnvironments,
		WorkerServiceClientFactory workerServiceClientFactory,
		ILibraryServiceClient libraryServiceClient,
		SchedulerConfiguration schedulerConfiguration
	) {
		super(workers, workerEnvironments, workerServiceClientFactory, libraryServiceClient, schedulerConfiguration);
		this.jobs = jobs;
		this.taskBuffer = taskBuffer;
		this.waitForAllTasks = waitForAllTasks();
	}

	/**
	 * Returns true if scheduling strategy should/must wait for all tasks.
	 * @return
	 */
	protected abstract boolean waitForAllTasks();

	/**
	 * Prepare internal structure for scheduling to serve method nextWorkerId().
	 * By this call, all tasks for active job are known.
	 *
	 * @param tasks - number of tasks of active job to schedule. nextWorkerId() will be called 'tasks' times.
	 */
	public abstract void prepareForSchedule(int tasks);

	/**
	 * Setter method to change scheduler behaviour in runtime.
	 *
	 * @param waitForAllTasks new value, true if has to wait, false if not.
	 */
	public void setWaitForAllTasks(boolean waitForAllTasks) {
		this.waitForAllTasks = waitForAllTasks;
	}

	/**
	 * Returns node id for scheduling of next task.
	 *
	 * @return - most suited node id for a next task.
	 */
	public abstract String nextWorkerId(List<String> rIds) throws ScheduleTaskException ;


	@Override
	public void addWorker(String nId, ServiceEndpointDTO endpoint)
	throws NodeCommunicationException {

		LOGGER.debug(
			"Trying to add new Worker with id \"{}\" and endpoint \"{}\" to scheduler.",
			nId,
			endpoint
		);
		addNode(nId, endpoint);

		try {
			IWorkerServiceClient workerServiceClient = getNodeClient(nId);
			List<Future<Void>> futures = new LinkedList<>();

			// Create all queues on Node
			LOGGER.debug("Creating all Queues on Worker \"{}\".", nId);
			for (String jId : jobs) {
				Future<Void> futureCreateQueue = workerServiceClient.createQueue(jId);
				futures.add(futureCreateQueue);
			}

			// Check states off all futures
			for (Future<Void> future : futures) {
				future.get();
			}
			LOGGER.info("Worker \"{}\" was added.", nId);


		} catch (ExecutionException | ClientCommunicationException | UnknownNodeException e) {
			LOGGER.error("Error while adding a Worker \"{}\" to Scheduler.", nId, e);
			throw new NodeCommunicationException(e);
		} catch (InterruptedException e) {
			LOGGER.error("Error while adding a Worker \"{}\" to Scheduler.", nId, e);
			Thread.currentThread().interrupt();
			throw new NodeCommunicationException(e);
		}
	}

	@Override
	public void removeWorker(String nId) {
		removeNode(nId);
	}

	@Override
	public final void addJob(String jId)
	throws NodeCommunicationException {
		if (!jobs.contains(jId)) {
			jobs.add(jId);

			taskBuffer.put(jId, Collections.synchronizedList(new LinkedList<>()));
			LOGGER.info("Added Job \"{}\" to scheduler.", jId);

			try {

				// Create queues for new job on every node.
				LOGGER.debug("Create Queue for Job \"{}\" on all Workers.", jId);

				List<Future<Void>> futures = new LinkedList<>();
				for (String nId : getNodes()) {
					IWorkerServiceClient workerClient = getNodeClient(nId);
					futures.add(workerClient.createQueue(jId));
				}

				// Create queues for new job on every node was requested.
				LOGGER.debug("Create Queue for Job \"{}\" on all Workers successfully requested.", jId);

				for (Future<Void> futureCreateQueue : futures) {
					futureCreateQueue.get();
				}

				LOGGER.info("Successfully created Queue \"{}\" on all Workers.", jId);

				// TODO: Reliable connection?
				// Remove node, if connection isn't possible?
			} catch (ClientCommunicationException | UnknownNodeException | ExecutionException e) {
				String msg = String.format("Error while create Queue \"%s\" on Workers.", jId);

				LOGGER.error(msg, e);
				throw new NodeCommunicationException(msg, e);
			} catch (InterruptedException e) {
				String msg = String.format("Error while create Queue \"%s\" on Workers.", jId);

				LOGGER.error(msg, e);
				Thread.currentThread().interrupt();
				throw new NodeCommunicationException(msg, e);
			}

		} else {
			LOGGER.warn("Job \"{}\" already known by this scheduler.", jId);
		}
	}



	@Override
	public final void removeJob(String jId)
	throws NodeCommunicationException {
		LOGGER.debug("Try to abort job with id \"{}\"", jId);
		if (!jobs.contains(jId)) {
			throw new NodeCommunicationException(
				new UnknownJobException("Job with id \"" + jId + "\" not known")
			);
		}

		jobs.remove(jId);
		taskBuffer.remove(jId);

		try {

			LOGGER.debug("Try to delete all queue belong to given Job from Workers.");
			List<Future<Void>> futures = new LinkedList<>();

			for (String nId : getNodes()) {
				IWorkerServiceClient workerServiceClient = getNodeClient(nId);
				Future<Void> future = workerServiceClient.deleteQueue(jId);
				futures.add(future);
			}

			for (Future<Void> futureCreateQueue : futures) {
				futureCreateQueue.get();
			}

			LOGGER.info("Removed Queue \"{}\", which belong to Job \"{}\", on all Workers.", jId, jId);

		} catch (InterruptedException e) {
			String msg = String.format("Error while remove Queue \"%s\" on Workers.", jId);
			LOGGER.error(msg, e);
			Thread.currentThread().interrupt();
			throw new NodeCommunicationException(msg, e);

		} catch (ExecutionException | UnknownNodeException | ClientCommunicationException e) {
			String msg = String.format("Error while remove Queue \"%s\" on Workers.", jId);
			LOGGER.error(msg, e);
			throw new NodeCommunicationException(msg, e);
		}
	}

	@Override
	public final void schedule(String jId, Collection<TaskDTO> tasks)
	throws ScheduleTaskException {
		doSchedule(
			!waitForAllTasks,
			jId,
			tasks
		);
	}

	@Override
	public final void markJobAsComplete(String jId)
	throws ScheduleTaskException {
		// Check if job is registered and flag waitForJobComplete is set
		if (jobs.contains(jId)) {
			if (waitForAllTasks) {
				// Schedule all buffered Tasks
				LOGGER.debug("Scheduling all Tasks of \"complete\" Job \"{}\" to Workers.", jId);

				doSchedule(true, jId, taskBuffer.get(jId));
				taskBuffer.get(jId).clear();
			}
		} else {
			throw new ScheduleTaskException(
				new UnknownJobException("Job with Id " + jId + " not known by this scheduler")
			);
		}
	}

	/**
	 * Helper method that encapsulates default scheduling logic.
	 *
	 * @param isImmediate flag that indicates when tasks should be submitted.
	 * @param jId job id.
	 * @param tasks tasks to be scheduled.
	 * @throws ScheduleTaskException
	 */
	protected void doSchedule(boolean isImmediate, String jId, Collection<TaskDTO> tasks)
	throws ScheduleTaskException {

		LOGGER.debug(
			"Job: \"{}\". Received {} Task(-s) for scheduling: [{}].",
			jId,
			tasks.size(),
			tasks.stream().map(TaskDTO::getId).toArray()
		);

		if (!jobs.contains(jId)) {
			String msg = String.format("Job with id \"%s\" not known, call addJob() first.", jId);
			LOGGER.error(msg);

			throw new ScheduleTaskException(
				new UnknownJobException(msg)
			);
		}

		// Check if tasks should/can be scheduled direct to a node or should be stored
		// until job will be marked as complete.

		if (isImmediate) {

			LOGGER.debug("Scheduling Tasks immediately to Workers.");

			// TODO: see Ticket "CLIENT COMMUNICATION / RELIABLE"
			// Naive assumption: all node services running, no network problems

			prepareForSchedule(tasks.size());
			for (TaskDTO taskDTO : tasks) {
                String wId = nextWorkerId(Arrays.asList(taskDTO.getObjectiveRoutineId(), taskDTO.getMapRoutineId()));
				scheduleTaskToWorker(wId, jId, taskDTO);
				LOGGER.info("Scheduled Task \"{}\" successfully to Worker \"{}\".", taskDTO.getId(), wId);
			}

			LOGGER.debug("Tasks were successfully scheduled.");

		} else {
			taskBuffer.get(jId).addAll(tasks);
			LOGGER.info("Buffer Tasks and wait for Job complete call.");
		}
	}

	/**
	 * Helper method that actually schedules a task to a node.
	 *
	 * @param nodeId - node id for scheduling
	 * @param queueId - queue id for scheduling
	 * @param task - task to schedule
	 */
	protected void scheduleTaskToWorker(String nodeId, String queueId, TaskDTO task)
	throws ScheduleTaskException {
		LOGGER.debug("Try to schedule Task {} on Worker {}.", task.getId(), nodeId);
		try {
			IWorkerServiceClient client = getNodeClient(nodeId);
			Future<Void> future = client.queueTasks(queueId, Collections.singletonList(task));
			future.get();
			LOGGER.debug("Scheduling task {} successfully executed on node {}.", task.getId(), nodeId);

		} catch (ExecutionException | UnknownNodeException | ClientCommunicationException e) {
			LOGGER.error("Error during scheduling Task {} on Worker {}.", task.getId(), nodeId, e);
			throw new ScheduleTaskException(e);
		} catch (InterruptedException e) {
			LOGGER.error("Error during scheduling Task {} on Worker {}.", task.getId(), nodeId, e);
			Thread.currentThread().interrupt();
			throw new ScheduleTaskException(e);
		}
	}

	/**
	 * Get list of registered node ids.
	 *
	 * @return list of node ids.
	 */
	protected List<String> getWorkers(List<String> rIds) throws ScheduleTaskException {
		if(rIds == null || rIds.isEmpty()) {
			List<String> mNodes = getNodes();
			if(mNodes == null || mNodes.isEmpty()) {
				throw new ScheduleTaskException("Could not schedule task - no registered node found.");
			}
			return mNodes;
		} else {
		    try {
                List<String> mNodes = getNodes(rIds);
                if(mNodes == null || mNodes.isEmpty()) {
                    throw new ScheduleTaskException("Could not schedule task - no supporting node found.");
                }
                return mNodes;
            } catch (ClientCommunicationException e) {
		        throw new ScheduleTaskException("Could not schedule task - error communicating with library.");
            }
		}
	}

	/**
	 * Returns a list of active jobs.
	 *
	 * @return
	 */
	@Override
	public List<String> getJobs() {
		return new LinkedList<>(jobs);
	}
}
