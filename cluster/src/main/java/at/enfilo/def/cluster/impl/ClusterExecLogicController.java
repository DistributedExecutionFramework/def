package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.api.NodeExecutionException;
import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.util.exception.LibraryException;
import at.enfilo.def.domain.exception.WrongRoutineTypeException;
import at.enfilo.def.execlogic.impl.ExecLogicException;
import at.enfilo.def.execlogic.impl.IExecLogicController;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.UnknownResourceException;
import at.enfilo.def.transfer.UnknownTaskException;
import at.enfilo.def.transfer.dto.*;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * This class contains the cluster logic for execution domain.
 *
 */
public class ClusterExecLogicController implements IExecLogicController {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ClusterExecLogicController.class);
	private static final Object INSTANCE_LOCK = new Object();

	private static ClusterExecLogicController instance;

	private final ClusterResource clusterResource;
	private final WorkerController workerController;
	private final ReducerController reducerController;
	private final ClientRoutineWorkerController clientRoutineWorkerController;
	private final LibraryController libraryController;

	private DomainController domainController;

	/**
	 * Singleton pattern.
	 * @return an instance of ExecutionController
	 */
	public static ClusterExecLogicController getInstance() {
		synchronized (INSTANCE_LOCK) {
			if (instance == null) {
				instance = new ClusterExecLogicController(
					ClusterResource.getInstance(),
					WorkerController.getInstance(),
					ReducerController.getInstance(),
					ClientRoutineWorkerController.getInstance(),
					LibraryController.getInstance(),
					new DomainController(Cluster.getInstance().getConfiguration())
				);
			}
			return instance;
		}
	}

	/**
	 * Hide constructor: singleton pattern
	 */
	private ClusterExecLogicController(
			ClusterResource clusterResource,
			WorkerController workerController,
			ReducerController reducerController,
			ClientRoutineWorkerController clientRoutineWorkerController,
			LibraryController libraryController,
			DomainController domainController
	) {
		this.clusterResource = clusterResource;
		this.workerController = workerController;
		this.reducerController = reducerController;
		this.clientRoutineWorkerController = clientRoutineWorkerController;
		this.libraryController = libraryController;
		this.domainController = domainController;
	}

	/**
	 * Returns all programs stored by manager/cluster.
	 *
	 * @param userId - user id
	 * @return list of program ids
	 * @throws Exception
	 */
	@Override
	public List<String> getAllPrograms(String userId) {
		return domainController.getAllPrograms(userId);
	}

	/**
	 * Returns all programs managed by this controller.
	 * @return collection of all programs
	 */
	Collection<ProgramDTO> getAllPrograms() {
		return domainController.getAllPrograms();
	}

	/**
	 * Creates a new program and returns the id of program.
	 * @return id of program
	 */
	@Override
	public String createProgram(String cId, String uId)
	throws ExecLogicException {
		LOGGER.info("Creating new program on cluster {} for user {}.", cId, uId);
		// Ignoring User on cluster side
		if (!cId.equals(clusterResource.getId())) {
			LOGGER.error("Given cluster cluster id {} and cluster id {} not matching.", cId, clusterResource.getId());
			throw new ExecLogicException("Wrong cluster id.");
		}
		return domainController.createProgram(uId);
	}


	/**
	 * Returns program info for given program id.
	 *
	 * @param pId - requested program id
	 * @return program info instance
	 * @throws UnknownProgramException if pId is not known
	 */
	@Override
	public ProgramDTO getProgram(String pId)
	throws UnknownProgramException {
		return domainController.getProgram(pId);
	}


	/**
	 * Delete program and all jobs, tasks and data belong to the given program id.
	 *
	 * @param pId - to delete
	 */
	@Override
	public void deleteProgram(String pId)
	throws UnknownProgramException, ExecLogicException {
		// First, try to abort program if anything is running.
		// Remove shared resources from all nodes
		List<String> sharedResources = domainController.getAllSharedResources(pId);
		deleteSharedResourcesFromNodes(sharedResources);

		List<String> jIds = domainController.getAllJobs(pId);
		for (String jId : jIds) {
			try {
				deleteJob(pId, jId);
			} catch (UnknownJobException e) {
				LOGGER.error(DEFLoggerFactory.createProgramContext(pId), "Error while delete Job of Program.", e);
				throw new ExecLogicException(e);
			}
		}
		domainController.deleteProgram(pId);
		LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Deleted Program.");
	}

	@Override
	public void abortProgram(String pId)
	throws ExecLogicException, UnknownProgramException {
		try {
			List<String> jobIds = domainController.getAllJobs(pId);
			for (String jId : jobIds) {
				abortJob(pId, jId);
			}
			domainController.abortProgram(pId);
			LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Aborted Program.");

		} catch (UnknownJobException e) {
			LOGGER.error(DEFLoggerFactory.createProgramContext(pId ), "Error while aborting job of program.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void updateProgramName(String pId, String name)
	throws UnknownProgramException {
		domainController.updateProgramName(pId, name);
		LOGGER.info(DEFLoggerFactory.createJobContext(pId), "Updated program name to {}.", name);
	}

	@Override
	public void updateProgramDescription(String pId, String description)
	throws UnknownProgramException {
		domainController.updateProgramDescription(pId, description);
		LOGGER.info(DEFLoggerFactory.createJobContext(pId), "Updated program description to {}.", description);
	}

	@Override
	public void startClientRoutine(String pId, String crId) throws ExecLogicException, UnknownProgramException {
		ProgramDTO program = domainController.getProgram(pId);
		String userId = program.getUserId();
		try {
			// Check if client routine is existing
			RoutineDTO clientRoutine = libraryController.fetchRoutine(crId);

			if (clientRoutine != null) {
				clientRoutineWorkerController.addUser(userId);
				domainController.setClientRoutineId(pId, crId);
				clientRoutineWorkerController.runProgram(domainController.getProgram(pId));
			}
		} catch (LibraryException e) {
			LOGGER.error(DEFLoggerFactory.createProgramContext(pId), "Client routine with id {} not found.", crId, e);
			throw new ExecLogicException(e);
		} catch (NodeExecutionException e) {
			LOGGER.error(DEFLoggerFactory.createProgramContext(pId), "Error while running program with client routine.", e);
			throw new ExecLogicException(e);
		}
	}

	/**
	 * Mark Program as finished.
	 *
	 * @param pId - program id
	 * @throws UnknownProgramException if pId is not known
	 */
	@Override
	public void markProgramAsFinished(String pId)
	throws UnknownProgramException {
		domainController.markProgramAsFinished(pId);
		LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Marked program as finished.");
	}

	/**
	 * Returns all jobs from the requested Program.
	 *
	 * @param pId - program id
	 * @return list of job ids, sorted by creation timestamp
	 */
	@Override
	public List<String> getAllJobs(String pId)
	throws UnknownProgramException {
		return domainController.getAllJobs(pId);
	}


	/**
	 * Creates a new Job in the given Program and returns id of new job.
	 *
	 * @param pId - program id
	 * @return - id of new job
	 * @throws UnknownProgramException if pId is not known
	 */
	@Override
	public String createJob(String pId)
	throws UnknownProgramException, ExecLogicException {
		if (domainController.isProgramAborted(pId)) {
			LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Ignore create job request, program already aborted.");
			throw new ExecLogicException("Program already aborted.");
		}
		String jId = domainController.createJob(pId);
		try {
			workerController.addJob(jId);
			attachMapRoutine(pId, jId, clusterResource.getDefaultMapRoutineId());
		} catch (NodeExecutionException | UnknownJobException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while delegating add job to workers.", e);
			throw new ExecLogicException(e);
		}

		LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "Created job successfully");
		return jId;
	}


	/**
	 * Returns JobDTO for the requested job.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @return job info
	 * @throws UnknownProgramException if pId is not known
	 * @throws UnknownJobException if jId is not known
	 */
	@Override
	public JobDTO getJob(String pId, String jId)
	throws UnknownProgramException, UnknownJobException {
		return domainController.getJob(pId, jId);
	}


	/**
	 * Deletes a job and all tasks and data.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @throws UnknownProgramException if pId is not known
	 * @throws UnknownJobException if jId is not known
	 */
	@Override
	public void deleteJob(String pId, String jId) throws UnknownProgramException, UnknownJobException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Try to delete Job and associated Task data.");
		// abort job before delete it
		abortJob(pId, jId);
		deleteJobOnNodes(pId, jId);
		domainController.deleteJob(pId, jId);
		LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "Deleted job.");
	}

	/**
	 * Abort the given job: set all unfinished task to failed, set job to failed,
	 * stop execution of scheduled tasks.
	 * @param pId - program id
	 * @param jId - job id to abort
	 * @throws UnknownProgramException if pId is not known
	 * @throws UnknownJobException if jId is not known
	 */
	@Override
	public void abortJob(String pId, String jId) throws UnknownProgramException, UnknownJobException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Try to abort Job");
		// first pause queue on all workers
		workerController.pauseQueue(jId);
		// collect running tasks, abort job, abort all running tasks
		Collection<String> runningTasks = domainController.getRunningTasksOfJob(pId, jId);
		domainController.abortJob(pId, jId);
		for (String tId : runningTasks) {
			try {
				workerController.abortTask(tId);
			} catch (NodeExecutionException e) {
				LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Error while abort Task on Worker node", e);
			}
		}
		LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "Aborted job.");
	}

	private void deleteJobOnNodes(String pId, String jId) throws UnknownProgramException, UnknownJobException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Notify scheduler(s) to delete Job.");
		try {
			workerController.deleteJob(jId);
		} catch (NodeExecutionException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while delegating remove job to workers.", e);
			throw new ExecLogicException(e);
		}

		// Delete ReduceJob if available and job is not finished
		if (domainController.getJobById(pId, jId).getState() != ExecutionState.SUCCESS && domainController.hasJobReduceRoutine(pId, jId)) {
			try {
				reducerController.deleteReduceJob(jId);
			} catch (NodeExecutionException e) {
				LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while delegating remove reduce job to reducers.", e);
				throw new ExecLogicException(e);
			}
		}
	}

	/**
	 * Returns associated map routine id of requested job.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @return map routine id
	 * @throws UnknownProgramException if pId is not known
	 * @throws UnknownJobException if jId is not known
	 */
	@Override
	public String getAttachedMapRoutine(String pId, String jId)
	throws UnknownProgramException, UnknownJobException {
		return domainController.getAttachedMapRoutine(pId, jId);
	}

	/**
	 * Set mapRoutineId to the given job.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param mapRoutineId - mapRoutineId to attach
	 * @throws UnknownProgramException if pId is not known
	 * @throws UnknownJobException if jId is not known
	 * @throws WrongRoutineTypeException if mapRoutineId is not a map routine
	 * @throws ExecutionException if exec of future get failed
	 * @throws InterruptedException if interrupt occured
	 */
	@Override
	public void attachMapRoutine(String pId, String jId, String mapRoutineId)
	throws UnknownProgramException, UnknownJobException, ExecLogicException {
		LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "Trying to attach MapRoutine {} to Job.", mapRoutineId);
		try {
			RoutineDTO mapRoutine = libraryController.fetchRoutine(mapRoutineId);
			domainController.attachMapRoutine(pId, jId, mapRoutine);
		} catch (LibraryException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while fetching routine from library.", e);
			throw new ExecLogicException(e);
		}
	}

	/**
	 * Returns attached reduce routine id.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @return reduce routine id
	 * @throws UnknownProgramException if pId is not known
	 * @throws UnknownJobException if jId is not known
	 */
	@Override
	public String getAttachedReduceRoutine(String pId, String jId)
	throws UnknownProgramException, UnknownJobException {
		return domainController.getAttachedReduceRoutine(pId, jId);
	}

	/**
	 * Attach reduce routine to the given job.
	 *
	 * @param pId - program id
	 * @param jId - job id to attach reduce routine
	 * @param reduceRoutineId - reduce routine
	 * @throws UnknownProgramException if pId is not known
	 * @throws UnknownJobException if jId is not known
	 * @throws ExecLogicException if reduceRoutineId is not a reduce routine
	 * 		   communication to library failed, exec of future get failed or if interrupt occurred.
	 */
	@Override
	public void attachReduceRoutine(String pId, String jId, String reduceRoutineId)
	throws UnknownProgramException, UnknownJobException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Trying to attach ReduceRoutine {} to Job.", reduceRoutineId);

		try {
			RoutineDTO reduceRoutine = libraryController.fetchRoutine(reduceRoutineId);
			domainController.attachReduceRoutine(pId, jId, reduceRoutine);

			LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Add ReduceRoutine to Scheduler.");
			reducerController.addReduceJob(getJob(pId, jId));
			LOGGER.info("ReduceRoutine {} successfully attached and reduce job started.", reduceRoutineId);
		} catch (NodeExecutionException e) {
			LOGGER.error("Error while delegating add reduce job to reducers.", e);
			throw new ExecLogicException(e);
		} catch (LibraryException e) {
			LOGGER.error("Error while fetching routine from library.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public List<String> getAllTasks(String pId, String jId, SortingCriterion sortingCriterion)
			throws UnknownProgramException, UnknownJobException, ExecLogicException {
		return domainController.getAllTasks(pId, jId, sortingCriterion);
	}


	/**
	 * Returns a list of task ids for the given program and job.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param state - {@link ExecutionState} of tasks
	 * @return list of task ids
	 * @throws UnknownProgramException if pId is not known
	 * @throws UnknownJobException if jId is not known
	 */
	@Override
	public List<String> getAllTasksWithState(String pId, String jId, ExecutionState state, SortingCriterion sortingCriterion)
	throws UnknownProgramException, UnknownJobException, ExecLogicException {
		return domainController.getAllTasksWithState(pId, jId, state, sortingCriterion);
	}


	/**
	 * Creates a new Task in the given program and job.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param routineInstance - objective routine id -> bound to task, including parameters
	 * @return new task id
	 * @throws UnknownProgramException if pId is not known
	 * @throws UnknownJobException if jId is not known
	 */
	@Override
	public String createTask(String pId, String jId, RoutineInstanceDTO routineInstance)
	throws UnknownProgramException, UnknownJobException, ExecLogicException {
		if (domainController.isJobAborted(pId, jId)) {
			LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "Ignore create task request, job already aborted.");
			throw new ExecLogicException("Job already aborted.");
		}
		String tId = domainController.createEmptyTask(pId, jId);
		try {
			// Fetch routine
			RoutineDTO objectiveRoutine = libraryController.fetchRoutine(routineInstance.getRoutineId());

			TaskDTO task = domainController.configureTask(pId, jId, tId, routineInstance, objectiveRoutine);

			// Schedule task
			LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, task.getId()), "Try to schedule task.");

			workerController.runTask(task);

			LOGGER.info(DEFLoggerFactory.createTaskContext(pId, jId, task.getId()), "Task created.");
			return task.getId();
		} catch (UnknownTaskException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Error while configuring new task.", e);
			throw new ExecLogicException(e);
		} catch (NodeExecutionException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Error while delegating run task to workers.", e);
			throw new ExecLogicException(e);
		} catch (LibraryException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Error while fetching routine form library.", e);
			throw new ExecLogicException(e);
		}
	}


	/**
	 * Returns task info for given task id.
	 *
	 * @param pId - program id
	 * @param jId - job id
	 * @param tId - task id
	 * @return - task object
	 * @throws UnknownProgramException if pId is not known
	 * @throws UnknownJobException if jId is not known
	 * @throws UnknownTaskException if tId is not known
	 */
	@Override
	public TaskDTO getTask(String pId, String jId, String tId)
	throws UnknownProgramException, UnknownJobException, UnknownTaskException, ExecLogicException {
		return domainController.getTask(pId, jId, tId);
	}

	@Override
	public TaskDTO getTaskPartial(String pId, String jId, String tId, boolean includeInParameters, boolean includeOutParameters)
	throws ExecLogicException, UnknownProgramException, UnknownJobException, UnknownTaskException {
		return domainController.getTaskPartial(pId, jId, tId, includeInParameters, includeOutParameters);
	}

	/**
	 * Marks the given job as complete. This means all tasks are created.
	 *
	 * @param pId - program id
	 * @param jId - task id
	 * @throws UnknownProgramException if pId is not known
	 * @throws UnknownJobException if jId is not known
	 */
	@Override
	public void markJobAsComplete(String pId, String jId)
	throws UnknownProgramException, UnknownJobException, ExecLogicException {
		domainController.markJobAsComplete(pId, jId);
		// Delegate mark job as complete to worker controller
		try {
			workerController.markJobAsComplete(jId);
		} catch (NodeExecutionException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while delegating mark job as complete to workers.", e);
			throw new ExecLogicException(e);
		}

		LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "Marked job as completed.");
	}

	@Override
	public void abortTask(String pId, String jId, String tId)
	throws UnknownProgramException, UnknownJobException, UnknownTaskException, ExecLogicException {
		domainController.abortTask(pId, jId, tId);

		// Delegate abort to NodeController.
		try {
			workerController.abortTask(tId);
		} catch (NodeExecutionException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Error while delegating abort task to workers.", e);
			throw new ExecLogicException(e);
		}
		LOGGER.info(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Aborted task.");
	}

	@Override
	public void reRunTask(String pId, String jId, String tId)
	throws UnknownProgramException, ExecLogicException, UnknownJobException, UnknownTaskException {
		TaskDTO task = domainController.fetchAndPrepareTaskForReRun(pId, jId, tId);
		try {
			workerController.runTask(task);
		} catch (NodeExecutionException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId),"Error while delegating re-run tasks on workers.", e);
			throw new ExecLogicException(e);
		}
		LOGGER.info(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Re-run of task scheduled.");
	}

	/**
	 * Returns a list of shared resources associated to given program.
	 * @param pId - program id
	 * @return list of shared resource ids
	 * @throws UnknownProgramException if pId is not known
	 */
	@Override
	public List<String> getAllSharedResources(String pId)
	throws UnknownProgramException {
		return domainController.getAllSharedResources(pId);
	}

	/**
	 * Creates a new shared resource in the given program.
	 *
	 * @param pId - program id
	 * @param dataTypeId - datatype of shared resource
	 * @return shared resource if
	 * @throws UnknownProgramException if pId not known
	 */
	@Override
	public String createSharedResource(String pId, String dataTypeId, ByteBuffer data)
	throws UnknownProgramException {
		ResourceDTO sharedResource = domainController.createSharedResource(pId, dataTypeId, data);
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Try to create shared resource on every attached node.");
		workerController.distributeSharedResource(sharedResource);
		reducerController.distributeSharedResource(sharedResource);
		LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Created shared resource.");
		return sharedResource.getId();
	}

	/**
	 * Returns shared resource incl. data.
	 *
	 * @param pId - program id
	 * @param rId - shared resource id
	 * @return shared resource
	 * @throws UnknownProgramException if pId not known
	 * @throws UnknownResourceException if rId not known
	 */
	@Override
	public ResourceDTO getSharedResource(String pId, String rId)
	throws UnknownProgramException, UnknownResourceException {
		return domainController.getSharedResource(pId, rId);
	}


	/**
	 * Delete and cleanup the requested shared resource.
	 * @param pId - program id
	 * @param rId - shared resource id
	 * @throws UnknownProgramException if pId not known
	 * @throws UnknownResourceException if rId not known
	 */
	@Override
	public void deleteSharedResource(String pId, String rId)
	throws UnknownProgramException {
		domainController.deleteSharedResource(pId, rId);
		deleteSharedResourcesFromNodes(Collections.singletonList(rId));
		LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Deleted shared resource {}.", rId);
	}

	private void deleteSharedResourcesFromNodes(List<String> sharedResources) {
		workerController.removeSharedResources(sharedResources);
		reducerController.removeSharedResources(sharedResources);
	}

	/**
	 * Notification from worker, that a list of tasks reached a new state.
	 * Fetch finished task from worker and update internal domain task.
	 *
	 * @param wId - worker id
	 * @param taskIds - list of task ids
	 * @param newState - new state
	 */
	void notifyTasksNewState(String wId, List<String> taskIds, ExecutionState newState)
	throws UnknownNodeException, UnknownProgramException, UnknownJobException, UnknownTaskException, ExecLogicException, NodeExecutionException {
		LOGGER.debug("Notify tasks have new state {} on worker {}.", newState, wId);
		switch (newState) {
			case SCHEDULED:
				domainController.notifyTasksChangedState(taskIds, ExecutionState.SCHEDULED);
				break;
			case RUN:
				domainController.notifyTasksChangedState(taskIds, ExecutionState.RUN);
				break;
			case SUCCESS:
				for (String tId : taskIds) {
					TaskDTO task = workerController.fetchFinishedTask(wId, tId);
					domainController.notifyTaskChangedState(task);

					// Reduce
					if (domainController.hasJobReduceRoutine(task.getProgramId(), task.getJobId())) {
						LOGGER.debug(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()), "Delegate task results to ReducerScheduler.");
						reducerController.scheduleResourcesToReduce(task.getJobId(), task.getOutParameters());

						if (domainController.allTasksOfJobSuccessful(task.getProgramId(), task.getJobId())) {
							JobDTO reducedJob = reducerController.finalizeReduce(task.getJobId());
							domainController.setReducedResultsOfJob(reducedJob.getProgramId(), reducedJob.getId(), reducedJob.getReducedResults());
							LOGGER.info(DEFLoggerFactory.createJobContext(task.getProgramId(), task.getJobId()), "Reduce done.");
						}
					}
				}
				break;
			case FAILED:
				for (String tId : taskIds) {
					TaskDTO task = workerController.fetchFinishedTask(wId, tId);
					domainController.notifyTaskChangedState(task);
				}
				break;
			default:
				// Ignoring new state
				break;
		}
	}

	void notifyProgramsNewState(String wId, List<String> programIds, ExecutionState newState)
	throws UnknownProgramException, UnknownNodeException, NodeExecutionException {
		LOGGER.debug("Notify programs have new state {} on client routine worker {}.", newState, wId);
		switch (newState) {
			case RUN:
				domainController.notifyProgramsRun(programIds);
				break;
			case SUCCESS:
				notifyProgramsSuccess(wId, programIds);
				break;
			case FAILED:
				// TODO
				notifyProgramsSuccess(wId, programIds);
				break;
			case SCHEDULED:
				// TODO
				break;
			default:
				// Ignoring new state
				break;
		}
	}

	private void notifyProgramsSuccess(String wId, List<String> programIds)
	throws UnknownProgramException, UnknownNodeException, NodeExecutionException {
		LOGGER.debug("Notify programs successful on client routine worker {}.", wId);
		for (String pId: programIds) {
			ProgramDTO program = clientRoutineWorkerController.fetchFinishedProgram(wId, pId);
			domainController.notifyProgramSuccess(program);
		}
	}

	/**
	 * Re-schedule the given tasks.
	 *
	 * @param taskIds - task (ids) to re-schedule
	 */
	void reScheduleTasks(Collection<String> taskIds) {
		LOGGER.info("Re-scheduling tasks.");
		for (String taskId : taskIds) {
			LOGGER.debug(DEFLoggerFactory.createTaskContext(taskId), "Trying to re-schedule task.");
			try {
				TaskDTO task = domainController.getTask(taskId);
				workerController.runTask(task);
			} catch (ExecLogicException | UnknownTaskException e) {
				LOGGER.error("Error while fetching task.", e);
			} catch (NodeExecutionException e) {
				LOGGER.error("Error while scheduling tasks on workers.", e);
			}
		}
	}

	void reScheduleReduceResources(String jId, Collection<String> resourceKeys) {
		LOGGER.info("Re-scheduling resources to reduce.");
		try {
			List<ResourceDTO> resourcesToSchedule = domainController.getResourcesWithSpecificKeys(jId, resourceKeys);
			LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Try to re-schedule resources of job.");
			reducerController.scheduleResourcesToReduce(jId, resourcesToSchedule);
		} catch (UnknownProgramException | UnknownJobException | UnknownTaskException | ExecLogicException e) {
			LOGGER.error("Error while fetching resources.", e);
		} catch (NodeExecutionException e) {
			LOGGER.error("Error while scheduling resources on reducers.", e);
		}
    }

    void reSchedulePrograms(Collection<String> programIds) {
		LOGGER.info("Re-scheduling programs.");
		for (String programId: programIds) {
			LOGGER.debug(DEFLoggerFactory.createProgramContext(programId), "Trying to re-schedule program");
			try {
				ProgramDTO program = domainController.getProgram(programId);
				clientRoutineWorkerController.runProgram(program);
			} catch (UnknownProgramException e) {
				LOGGER.error("Error while fetching program.", e);
			} catch (NodeExecutionException e) {
				LOGGER.error("Error while scheduling program on client routine workers.", e);
			}
		}
	}
}