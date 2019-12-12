package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.domain.entity.Job;
import at.enfilo.def.domain.entity.Program;
import at.enfilo.def.domain.entity.Routine;
import at.enfilo.def.domain.entity.User;
import at.enfilo.def.domain.exception.JobCompletedException;
import at.enfilo.def.domain.exception.WrongRoutineTypeException;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.dto.cache.UnknownCacheObjectException;
import at.enfilo.def.execlogic.impl.ExecLogicException;
import at.enfilo.def.execlogic.impl.IExecLogicController;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.UnknownResourceException;
import at.enfilo.def.transfer.UnknownTaskException;
import at.enfilo.def.transfer.dto.*;
import at.enfilo.def.transfer.util.MapManager;
import at.enfilo.def.transfer.util.ResourceUtil;
import at.enfilo.def.worker.api.IWorkerServiceClient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * This class contains the cluster logic for execution domain.
 *
 */
public class ClusterExecLogicController implements IExecLogicController {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ClusterExecLogicController.class);
	private static final String UNKNOWN_PROGRAM = "Program with id %s not known by this cluster";
	private static final Object INSTANCE_LOCK = new Object();
	static final String DTO_TASK_CACHE_CONTEXT = "cluster-tasks";

	private static ClusterExecLogicController instance;

	private final WorkerController workerController;
	private final Map<String, Program> programMap;
	private final DTOCache<TaskDTO> taskCache;
	private final ClusterResource clusterResource;
	private final Object deleteLock;
	private ILibraryServiceClient libraryServiceClient;

	/**
	 * Singleton pattern.
	 * @return an instance of ExecutionController
	 */
	public static ClusterExecLogicController getInstance() {
		synchronized (INSTANCE_LOCK) {
			if (instance == null) {
				instance = new ClusterExecLogicController(
						WorkerController.getInstance(),
						null, // Null means, that LibraryServiceClient will be constructed (Endpoint is defined in ClusterConfiguration)
						new HashMap<>()
				);
			}
			return instance;
		}
	}


	/**
	 * Hide constructor: singleton pattern
	 */
	private ClusterExecLogicController(
			WorkerController workerController,
			ILibraryServiceClient libraryServiceClient,
			Map<String, Program> programMap
	) {
		if (libraryServiceClient == null) {
			LibraryServiceClientFactory factory = new LibraryServiceClientFactory();
			try {
				this.libraryServiceClient = factory.createClient(Cluster.getInstance().getConfiguration().getLibraryEndpoint());
			} catch (ClientCreationException e) {
				LOGGER.error("Error while create ILibraryServiceClient.", e);
			}
		} else {
			this.libraryServiceClient = libraryServiceClient;
		}
		this.programMap = programMap;
		this.workerController = workerController;
		this.taskCache = DTOCache.getInstance(DTO_TASK_CACHE_CONTEXT, TaskDTO.class);
		this.clusterResource = ClusterResource.getInstance();
		this.deleteLock = new Object();
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
		LOGGER.debug("Fetching all programs with userId {}.", userId);
		return programMap.values().stream()
				.filter(program -> program.getOwner().getId().toLowerCase().matches(userId.toLowerCase()))
				.map(Program::getId)
				.collect(Collectors.toList());
	}

	/**
	 * Returns Program by its id
	 * @param pId - program id
	 * @return program instance
	 * @throws UnknownProgramException if pId is not known
	 */
	private Program getProgramById(String pId) throws UnknownProgramException {
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Fetching program");
		if (!programMap.containsKey(pId)) {
			LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Program id is not contained in program map.");
			throw new UnknownProgramException(String.format(UNKNOWN_PROGRAM, pId));
		}
		return programMap.get(pId);
	}

	/**
	 * Creates a new program and returns the id of program.
	 * @return id of program
	 */
	@Override
	public String createProgram(String cId, String uId)
	throws ExecLogicException {
		LOGGER.debug("Creating new program on cluster with id {} for user with id {}.", cId, uId);
		// Ignoring User on cluster side
		if (!cId.equals(clusterResource.getId())) {
			LOGGER.error("Given cluster cluster id {} and cluster id {} not matching.", cId, clusterResource.getId());
			throw new ExecLogicException("Wrong cluster id.");
		}
		Program program = new Program();
		program.setOwner(new User(uId));
		program.setState(ExecutionState.SCHEDULED);
		programMap.put(program.getId(), program);
		LOGGER.debug("Program {} successfully created.", program.getId());
		return program.getId();
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
	throws UnknownProgramException, ExecLogicException {
		return MapManager.map(getProgramById(pId), ProgramDTO.class);
	}


	/**
	 * Delete program and all jobs, tasks and data belong to the given program id.
	 *
	 * @param pId - to delete
	 */
	@Override
	public void deleteProgram(String pId) throws UnknownProgramException {
		// First, try to abort program if anything is running.
		synchronized (deleteLock) {
			LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Deleting program.");
			Program program = getProgramById(pId);
			try {
				if (program.getState() == ExecutionState.RUN || program.getState() == ExecutionState.SCHEDULED) {
					LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Try to abort Program.");
					abortProgram(pId);
				}
			} catch (ExecLogicException e) {
				LOGGER.error(DEFLoggerFactory.createProgramContext(pId), "Error while abort Program.", e);
			}

			// Remove shared resources from all nodes
			List<String> sharedResources = program.getSharedResources()
					.stream()
					.map(ResourceDTO::getId)
					.collect(Collectors.toList());
			workerController.removeSharedResources(sharedResources);

			List<String> jIds = program.getJobs().stream()
					.map(Job::getId)
					.collect(Collectors.toList());
			for (String jId : jIds) {
				//job.getAllTasks().forEach(taskCache::remove);
				try {
					deleteJob(pId, jId);
				} catch (UnknownJobException e) {
					LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Cannot remove unknown Job {}.", jId, e);
				}
			}
			program.cleanUp();
			programMap.remove(pId);
			LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Deleted Program.");
		}
	}

	@Override
	public void abortProgram(String pId) throws ExecLogicException, UnknownProgramException {
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Aborting Program.");
		Program program = getProgramById(pId);

		try {
			for (Job job : program.getJobs()) {
				if (job.getState() == ExecutionState.SCHEDULED || job.getState() == ExecutionState.RUN) {
					abortJob(pId, job.getId());
				}
			}
		} catch (UnknownJobException e) {
			LOGGER.error(DEFLoggerFactory.createProgramContext(pId), "Error while abort Program.", e);
		}

		program.abort();
		LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Program aborted.");
	}

	@Override
	public void updateProgramName(String pId, String name) throws ExecLogicException, UnknownProgramException {
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Updating name of Program: {}.", name);
		Program program = getProgramById(pId);
		program.setName(name);
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Updated Program name.");
	}

	@Override
	public void updateProgramDescription(String pId, String description) throws ExecLogicException, UnknownProgramException {
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Updating description of Program: {}.", description);
		Program program = getProgramById(pId);
		program.setDescription(description);
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Updated program description.");
	}

	/**
	 * Mark Program as finished.
	 *
	 * @param pId - program id
	 * @throws UnknownProgramException if pId is not known
	 */
	@Override
	public void markProgramAsFinished(String pId)
	throws UnknownProgramException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Marking Program as finished.");
		Program program = getProgramById(pId);
		program.markAsFinished();
		LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Marked Program as finished.");
	}


	/**
	 * Returns all jobs from the requested Program.
	 *
	 * @param pId - program id
	 * @return list of job ids, sorted by creation timestamp
	 */
	@Override
	public List<String> getAllJobs(String pId)
	throws UnknownProgramException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Fetching all Jobs of Program.");
		Program program = getProgramById(pId);
		return program
				.getJobs()
				.stream().sorted(Comparator.comparing(Job::getCreateTime))
				.map(Job::getId)
				.collect(Collectors.toList());
	}


	/**
	 * Creates a new Job in the given Program and returns id of new job.
	 *
	 * @param pId - program id
	 * @return - id of new job
	 * @throws UnknownProgramException if pId is not known
	 */
	@Override
	public String createJob(String pId) throws UnknownProgramException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Creating Job in Program");
		Program program = getProgramById(pId);
		if (program.getState() == ExecutionState.FAILED) {
			LOGGER.warn(DEFLoggerFactory.createProgramContext(pId), "Can not create a Job in a aborted Program.");
			throw new ExecLogicException(String.format("Program %s is already aborted. Can not create a new Job.", pId));
		}
		Job job = new Job();
		job.setState(ExecutionState.SCHEDULED);
		program.addJob(job);
		LOGGER.info(DEFLoggerFactory.createJobContext(pId, job.getId()), "Created new Job in Program.");

		String defaultMapRoutineId = clusterResource.getDefaultMapRoutineId();
		try {
			RoutineDTO defaultMapRoutine = libraryServiceClient.getRoutine(defaultMapRoutineId).get();
			job.setMapRoutine(MapManager.map(defaultMapRoutine, Routine.class));
		} catch (InterruptedException | ExecutionException | WrongRoutineTypeException | ClientCommunicationException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(pId, job.getId()), "Error while set DefaultMapRoutine {} to new Job.", defaultMapRoutineId, e);
		}

		try {
			Future<Void> futureAddJob = clusterResource
					.getSchedulerServiceClient(NodeType.WORKER)
					.addJob(job.getId());
			futureAddJob.get(); // Wait for finished

			LOGGER.info(DEFLoggerFactory.createJobContext(pId, job.getId()), "Added Job successfully on Scheduler.");
		} catch (ClientCommunicationException | InterruptedException | ExecutionException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(pId, job.getId()), "Error while add Job on Scheduler", e);
		}
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, job.getId()), "Created Job successfully.");
		return job.getId();
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
	throws UnknownProgramException, UnknownJobException, ExecLogicException {
		Program program = getProgramById(pId);
		Job job = program.getJobById(jId);
		return MapManager.map(job, JobDTO.class);
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
	public void deleteJob(String pId, String jId)
	throws UnknownProgramException, UnknownJobException {
		synchronized (deleteLock) {
			LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Try to delete Job and associated Task data.");
			Program program = getProgramById(pId);
			Job job = program.getJobById(jId);
			job.getAllTasks().forEach(taskCache::remove);
			job.cleanUp();
			program.deleteJob(job);
			try {
				LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Notify scheduler(s) to delete Job.");
				Future<Void> futureDeleteJob = clusterResource.getSchedulerServiceClient(NodeType.WORKER).removeJob(jId);
				if ((job.getState() == ExecutionState.RUN || job.getState() == ExecutionState.SCHEDULED)
						&& job.hasReduceRoutine()) {
					// Ignoring returned future, because scheduler service client could be same as before.
					clusterResource.getSchedulerServiceClient(NodeType.REDUCER).removeJob(jId);
				}

				futureDeleteJob.get(); // Wait for ticket
				LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "Deleted Job successfully.");
			} catch (ExecutionException | ClientCommunicationException e) {
				LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while delete Job from Scheduler.", e);
			} catch (InterruptedException e) {
				LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while delete Job from Scheduler.", e);
				Thread.currentThread().interrupt();
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
	throws UnknownProgramException, UnknownJobException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Fetching attached MapRoutine.");
		Program program = getProgramById(pId);
		Job job = program.getJobById(jId);
		if (job.hasMapRoutine()) {
			// TODO: Should not be null -> default map routine
			LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Fetched attached MapRoutine {}.", job.getMapRoutine().getId());
			return job.getMapRoutine().getId();
		}
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Job has no MapRoutine attached.");
		return null;
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
	 * @throws ClientCommunicationException if communication to library failed
	 * @throws ExecutionException if exec of future get failed
	 * @throws InterruptedException if interrupt occured
	 */
	@Override
	public void attachMapRoutine(String pId, String jId, String mapRoutineId)
	throws UnknownProgramException, UnknownJobException, ExecLogicException {
		LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "Try to attach MapRoutine {}.", mapRoutineId);

		try {
			Program program = getProgramById(pId);
			Job job = program.getJobById(jId);
			Future<RoutineDTO> futureRoutine = libraryServiceClient.getRoutine(mapRoutineId);
			Routine mapRoutine = MapManager.map(futureRoutine.get(), Routine.class);
			job.setMapRoutine(mapRoutine);
			LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "MapRoutine {} successfully attached.", mapRoutineId);

		} catch (InterruptedException | ExecutionException | ClientCommunicationException |
				WrongRoutineTypeException e) {

			LOGGER.error("Error while attach MapRoutine {}.", mapRoutineId, e);
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
	throws UnknownProgramException, UnknownJobException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Fetching attached ReduceRoutine.");
		Program program = getProgramById(pId);
		Job job = program.getJobById(jId);
		if (job.hasReduceRoutine()) {
			LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Fetched ReduceRoutine {}.", job.getReduceRoutine().getId());
			return job.getReduceRoutine().getId();
		}
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Job has no ReduceRoutine attached.");
		return null;
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
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Try to attach ReduceRoutine.");

		try {
			Program program = getProgramById(pId);
			Job job = program.getJobById(jId);
			Future<RoutineDTO> futureRoutine = libraryServiceClient.getRoutine(reduceRoutineId);
			Routine reduceRoutine = MapManager.map(futureRoutine.get(), Routine.class);
			job.setReduceRoutine(reduceRoutine);

			LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Add ReduceRoutine to Scheduler.");
			Future<Void> futureAddReduce = clusterResource.getReducerSchedulerServiceClient().extendToReduceJob(jId, reduceRoutineId);
			futureAddReduce.get(); // Wait for done

			LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "ReduceRoutine {} successfully attached.", reduceRoutineId);

		} catch (InterruptedException | ExecutionException | ClientCommunicationException | WrongRoutineTypeException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while attach ReduceRoutine {}.", reduceRoutineId, e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public List<String> getAllTasks(String pId, String jId, SortingCriterion sortingCriterion) throws ExecLogicException, UnknownProgramException, UnknownJobException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Fetching all Tasks.");
		Program program = getProgramById(pId);
		Job job = program.getJobById(jId);
		List<String> taskIds = new LinkedList<>(job.getAllTasks());
		return sortTasks(taskIds, sortingCriterion);
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
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Fetching all Tasks with state {}.", state);
		Program program = getProgramById(pId);
		Job job = program.getJobById(jId);
		List<String> taskIds = new LinkedList<>();
		switch (state) {
			case SCHEDULED:
				taskIds.addAll(job.getScheduledTasks());
				break;
			case RUN:
				taskIds.addAll(job.getRunningTasks());
				break;
			case SUCCESS:
				taskIds.addAll(job.getSuccessfulTasks());
				break;
			case FAILED:
				taskIds.addAll(job.getFailedTasks());
				break;
		}
		return sortTasks(taskIds, sortingCriterion);
	}

	private List<String> sortTasks(List<String> taskIds, SortingCriterion sortingCriterion) {
		LOGGER.debug("Sorting all tasks with criterion {}.", sortingCriterion);
		if (sortingCriterion == SortingCriterion.NO_SORTING) {
			return taskIds;
		}
		List<TaskDTO> tasks = new LinkedList<>();
		try {
			for (String taskId : taskIds) {
				TaskDTO task = taskCache.fetch(taskId);
				task.setRuntime(calcRuntimeForTask(task));
				tasks.add(task);
			}
		} catch (UnknownCacheObjectException | IOException e) {
			LOGGER.error("Error while fetching all Tasks to sort from DTOCache:", e);
		}

		return tasks.stream()
				.sorted((t1, t2) -> compareTasks(t1, t2, sortingCriterion))
				.map(TaskDTO::getId)
				.collect(Collectors.toList());
	}

	private int compareTasks(TaskDTO t1, TaskDTO t2, SortingCriterion sortingCriterion) {
		switch (sortingCriterion) {
			case CREATION_DATE_FROM_NEWEST:
				return compareByTime(t1.getCreateTime(), t2.getCreateTime());
			case CREATION_DATE_FROM_OLDEST:
				return compareByTime(t2.getCreateTime(), t1.getCreateTime());
			case START_DATE_FROM_NEWEST:
				return compareByTime(t1.getStartTime(), t2.getStartTime());
			case START_DATE_FROM_OLDEST:
				return compareByTime(t2.getStartTime(), t1.getStartTime());
			case FINISH_DATE_FROM_NEWEST:
				return compareByTime(t1.getFinishTime(), t2.getFinishTime());
			case FINISH_DATE_FROM_OLDEST:
				return compareByTime(t2.getFinishTime(), t1.getFinishTime());
			case RUNTIME_FROM_LONGEST:
				return compareByTime(t2.getRuntime(), t1.getRuntime());
			case RUNTIME_FROM_SHORTEST:
				return compareByTime(t1.getRuntime(), t2.getRuntime());
			default:
				return -1;
		}
	}

	private int compareByTime(long firstTime, long secondTime) {
		if (firstTime > secondTime) {
			return -1;
		} else if (firstTime == secondTime) {
			return 0;
		}
		return 1;
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
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Try to create Task.");

		// Create Task and register it
		Program program = getProgramById(pId);
		Job job = program.getJobById(jId);
		TaskDTO task = new TaskDTO();
		task.setId(UUID.randomUUID().toString());
		task.setJobId(jId);
		task.setProgramId(pId);
		task.setCreateTime(System.currentTimeMillis());
		task.setState(ExecutionState.SCHEDULED);

		try {
			// Should be very early, to be sure that markAsComplete request is not performed yet
			job.addTask(task.getId());
			taskCache.cache(task.getId(), task);

			// Check input
			if (routineInstance.isSetMissingParameters() && !routineInstance.getMissingParameters().isEmpty()) {
				String msg = "Missing Parameters must be empty at RoutineInstance";
				LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), msg);
				throw new ExecLogicException(msg);
			}

			// Fetch and cache Routine
			RoutineDTO objectiveRoutine = libraryServiceClient.getRoutine(routineInstance.getRoutineId()).get();

			// Check Routine and Routine Parameters against RoutineInstance
			for (FormalParameterDTO formalParameter : objectiveRoutine.getInParameters()) {
				// Check for all needed parameters
				if (routineInstance.getInParameters().containsKey(formalParameter.getName())) {
					ResourceDTO paramResource = routineInstance.getInParameters().get(formalParameter.getName());
					String dataTypeId;
					if (ResourceUtil.isSharedResource(paramResource)) {
						// SharedResource case, replace dataTypeId with SharedResource one.
						try {
							dataTypeId = getSharedResource(pId, paramResource.getId()).getDataTypeId();
						} catch (UnknownResourceException ex) {
							String msg = String.format("SharedResource %s not found for parameter '%s'.", paramResource.getId(), formalParameter.getName());
							LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, task.getId()), msg);
							throw new ExecLogicException(msg);
						}
					} else {
						dataTypeId = paramResource.getDataTypeId();
					}
					// Check for correct DataType
					if (!formalParameter.getDataType().getId().equals(dataTypeId)) {
						String msg = String.format(
								"Parameter %s has wrong DataType: needed %s, given %s.",
								formalParameter.getName(),
								formalParameter.getDataType().getId(),
								dataTypeId
						);
						LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, task.getId()), msg);
						throw new ExecLogicException(msg);
					}

				} else {
					String msg = String.format(
							"Parameter %s needed by Routine %s and is missing at RoutineInstance",
							formalParameter.getName(),
							objectiveRoutine.getId()
					);
					LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, task.getId()), msg);
					throw new ExecLogicException(msg);
				}
			}

			// Assembly task and cache it
			task.setObjectiveRoutineId(objectiveRoutine.getId());
			task.setMapRoutineId(job.getMapRoutine().getId());
			task.setInParameters(routineInstance.getInParameters());
			taskCache.cache(task.getId(), task);
			LOGGER.info(DEFLoggerFactory.createTaskContext(pId, jId, task.getId()), "Task (#{}) created.", job.getNumberOfTasks());

			// Schedule task
			LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, task.getId()), "Try to schedule Task.");
			Future<Void> futureSchedule = clusterResource
					.getSchedulerServiceClient(NodeType.WORKER)
					.scheduleTask(jId, task);
			futureSchedule.get(); // Wait for done
			LOGGER.info(DEFLoggerFactory.createTaskContext(pId, jId, task.getId()), "Task delegated to Scheduler.");
			return task.getId();

		} catch (JobCompletedException | ExecutionException | InterruptedException |
				ClientCommunicationException | ExecLogicException e) {
			// Remove Task from job.
			job.removeTask(task.getId());

			LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, task.getId()), "Error while creating Task.", e);
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
		LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Fetching Task.");
		try {
			TaskDTO task = taskCache.fetch(tId);
			task.setRuntime(calcRuntimeForTask(task));
			return task;

		} catch (IOException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Error while fetch Task.", e);
			throw new ExecLogicException(e);
		} catch (UnknownCacheObjectException e) {
			throw new UnknownTaskException(e.getMessage());
		}
	}

	@Override
	public TaskDTO getTaskPartial(String pId, String jId, String tId, boolean includeInParameters, boolean includeOutParameters) throws ExecLogicException, UnknownProgramException, UnknownJobException, UnknownTaskException {
		LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Fetching partial Task: inParams={}, outParams={}", includeInParameters, includeOutParameters);
		TaskDTO task = getTask(pId, jId, tId);
		if (includeInParameters && includeOutParameters) {
			return task;
		}
		TaskDTO partial = task.deepCopy();
		if (!includeInParameters) {
			partial.setInParameters(null);
		}
		if (!includeOutParameters) {
			partial.setOutParameters(null);
		}
		return partial;
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
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Marking Job as complete.");
		Program program = getProgramById(pId);
		Job job = program.getJobById(jId);
		job.markAsComplete();
		LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "Marked Job as completed.");
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
	public void abortJob(String pId, String jId)
	throws UnknownProgramException, UnknownJobException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(pId, jId), "Try to abort Job.");
		Program program = getProgramById(pId);
		Job job = program.getJobById(jId);
		if (job.getState() != ExecutionState.SUCCESS) {
			job.setState(ExecutionState.FAILED);
			try {
				Future<Void> futureAbortJob = clusterResource
						.getSchedulerServiceClient(NodeType.WORKER)
						.removeJob(jId);

				// Abort running tasks
				for (String tId : job.getRunningTasks()) {
					abortTask(pId, jId, tId);
				}

				futureAbortJob.get(); // Wait for done
				LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "Abort Job successfully on Scheduler.");

				// Set states of tasks
				for (String tId : job.getScheduledTasks()) {
					TaskDTO task = taskCache.fetch(tId);
					task.setState(ExecutionState.FAILED);
					task.setRuntime(calcRuntimeForTask(task));
					LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Notify job that task changed state from SCHEDULED to FAILED.");
					job.notifyTaskChangedState(tId, ExecutionState.SCHEDULED, ExecutionState.FAILED);
				}

			} catch (IOException | UnknownTaskException | UnknownCacheObjectException e) {
				LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while abort Job.", e);
			} catch (ClientCommunicationException | InterruptedException | ExecutionException e) {
				LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), "Error while aborting Job on Scheduler.", e);
			}

			job.abort();
			LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "Job aborted.");
		} else {
			LOGGER.info(DEFLoggerFactory.createJobContext(pId, jId), "Ignoring abort request because Job is already finished.");
		}
	}



	@Override
	public void abortTask(String pId, String jId, String tId)
	throws UnknownProgramException, UnknownJobException, UnknownTaskException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Aborting Task");
		// Check pId, jId and tId
		Program program = programMap.get(pId);
		if (program == null) {
			String msg = String.format("Abort Task: Program %s not known by this cluster.", pId);
			LOGGER.error(DEFLoggerFactory.createProgramContext(pId), msg);
			throw new UnknownProgramException(msg);
		}
		if (program.getJobById(jId) == null) {
			String msg = String.format("Abort Task: Job %s not part of Program %s.", jId, pId);
			LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), msg);
			throw new UnknownJobException(msg);
		}
		try {
			TaskDTO task = taskCache.fetch(tId);
			task.setState(ExecutionState.FAILED);
			task.setFinishTime(Instant.now().toEpochMilli());
			task.setRuntime(calcRuntimeForTask(task));
			taskCache.cache(task.getId(), task);
		} catch (IOException | UnknownCacheObjectException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Error while fetch Task from cache.", e);
		}

		try {
			taskCache.fetch(tId).setState(ExecutionState.FAILED);
		} catch (IOException | UnknownCacheObjectException e) {
			LOGGER.warn(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Can not update/abort Task state.", e);
		}

		// Delegate abort to NodeController.
		workerController.abortTask(tId);
		LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Aborted Task.");
	}



	@Override
	public void reRunTask(String pId, String jId, String tId)
	throws UnknownProgramException, ExecLogicException, UnknownJobException, UnknownTaskException {
		LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Re-running Task.");
		// Check pId, jId and tId
		Program program = programMap.get(pId);
		if (program == null) {
			String msg = String.format("Re-run Task: Program %s not known by this Cluster.", pId);
			LOGGER.error(DEFLoggerFactory.createProgramContext(pId), msg);
			throw new UnknownProgramException(msg);
		}
		Job job = program.getJobById(jId);
		if (job == null) {
			String msg = String.format("Re-run Task: Job %s not part of Program %s.", jId, pId);
			LOGGER.error(DEFLoggerFactory.createJobContext(pId, jId), msg);
			throw new UnknownJobException(msg);
		}
		if (!job.containsTask(tId)) {
			String msg = String.format("Re-run Task: Task is not part of Job %s.", jId);
			LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), msg);
			throw new UnknownTaskException(msg);
		}

		// Fetch task from cache, set state to scheduled and delegate task to scheduler.
		try {
			TaskDTO task = taskCache.fetch(tId);
			if (task.getState() == ExecutionState.SUCCESS || task.getState() == ExecutionState.FAILED) {
				LOGGER.debug(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Try to re-run Task.");
				job.notifyTaskChangedState(tId, task.getState(), ExecutionState.SCHEDULED);
				task.setState(ExecutionState.SCHEDULED);
				taskCache.cache(task.getId(), task); // update cache
				Future<Void> futureScheduleTask = clusterResource
						.getSchedulerServiceClient(NodeType.WORKER)
						.scheduleTask(jId, task);
				LOGGER.info(
						DEFLoggerFactory.createTaskContext(pId, jId, tId),
						"Re-run (schedule) finished with state {}.",
						futureScheduleTask.get()
				);
			} else {
				LOGGER.info(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Task must be in state {} or {} for re-run. Ignoring request.", ExecutionState.SUCCESS, ExecutionState.FAILED);
			}
		} catch (IOException | UnknownCacheObjectException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Error while fetch Task for re-run.", e);
			throw new ExecLogicException(e);
		} catch (ClientCommunicationException | InterruptedException | ExecutionException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(pId, jId, tId), "Re-run Task: Error while scheduling.");
			throw new ExecLogicException(e);
		}
	}


	/**
	 * Returns a list of shared resources associated to given program.
	 * @param pId - program id
	 * @return list of shared resource ids
	 * @throws UnknownProgramException if pId is not known
	 */
	@Override
	public List<String> getAllSharedResources(String pId) throws UnknownProgramException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Fetching all SharedResoucres.");
		Program program = getProgramById(pId);
		return program
				.getSharedResources()
				.stream()
				.map(ResourceDTO::getId)
				.collect(Collectors.toList());
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
	throws UnknownProgramException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Creating a SharedResource.");
		Program program = getProgramById(pId);
		ResourceDTO sharedResource = new ResourceDTO(UUID.randomUUID().toString(), dataTypeId);
		sharedResource.setData(data);
		program.addSharedResource(sharedResource);
		LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Created a SharedResource {}.", sharedResource.getId());
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Try to create SharedResource on every attached node.");
		workerController.distributeSharedResource(sharedResource);
		// TODO Reducer controller
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
	throws UnknownProgramException, UnknownResourceException, ExecLogicException {
		LOGGER.debug(DEFLoggerFactory.createProgramContext(pId), "Fetching SharedResource with id {}.", rId);
		Program program = getProgramById(pId);
		return program.getSharedResourceById(rId);
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
	throws UnknownProgramException, UnknownResourceException, ExecLogicException {
		LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Try to delete SharedResource {}.", rId);
		Program program = getProgramById(pId);
		// TODO Remove from nodes...
		program.deleteSharedResource(rId);
		LOGGER.info(DEFLoggerFactory.createProgramContext(pId), "Deleted SharedResource {}.", rId);
	}


	/**
	 * Returns all programs managed by this controller.
	 * @return collection of all programs
	 */
	Collection<Program> getAllPrograms() {
		LOGGER.debug("Fetching all Programs.");
		return programMap.values();
	}


	/**
	 * Notification from worker, that a list of tasks reached a new state.
	 * Fetch finished task from worker and update internal domain task.
	 *
	 * @param wId - worker id
	 * @param taskIds - list of task ids
	 * @param newState - new state
	 */
	void notifyTasksNewState(String wId, List<String> taskIds, ExecutionState newState) {
		LOGGER.debug("Notify Tasks have new state {} from Worker with id {}.", newState, wId);
		switch (newState) {
			case RUN:
				notifyTasksRun(taskIds);
				break;
			case SUCCESS:
				notifyTasksSuccess(wId, taskIds);
				break;
			case FAILED:
				// TODO
				notifyTasksSuccess(wId, taskIds);
				break;
			case SCHEDULED:
				// TODO
				break;
			default:
				// Ignoring new state
				break;
		}
	}


	private void notifyTasksSuccess(String wId, List<String> taskIds) {
		LOGGER.debug("Notify Tasks successfully from worker with id {}.", wId);
		for (String tId : taskIds) {
			try {
				// TODO: First check if finished task should be fetched from worker or directly passed to a reducer.
				IWorkerServiceClient workerServiceClient = workerController.getServiceClient(wId);
				TaskDTO task = workerServiceClient.fetchFinishedTask(tId).get();
				if (task.getState() == ExecutionState.RUN) {
					// TODO: this should not happen.
					task.setState(ExecutionState.SUCCESS);
					LOGGER.warn(
							DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()),
							"Fetched task was in state RUN instead of SUCCESS. Task state set to SUCCESS."
					);
				}
				Program program = programMap.get(task.getProgramId());
				Job job = program.getJobById(task.getJobId());
				ExecutionState oldTaskState = taskCache.fetch(tId).getState();

				// Update Cache
				taskCache.cache(task.getId(), task); // Update cache

				if (task.getState() == ExecutionState.SUCCESS) {
					LOGGER.info(
							DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()),
							"Task ({}/{}) finished successfully, all results fetched from worker.",
							job.getSuccessfulTasks().size(),
							job.getNumberOfTasks()
					);
					if (job.hasReduceRoutine()) {
						LOGGER.debug(
								DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()),
								"Delegate Task results to ReduceScheduler."
						);
						clusterResource.getReducerSchedulerServiceClient().scheduleResource(
								task.getJobId(),
								task.getOutParameters()
						);
					}
				} else {
					LOGGER.warn(
							DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()),
							"Task finished with state {} instead of {}.", task.getState(), ExecutionState.SUCCESS
					);
				}

				// Update Job - sets also job to finished if all tasks are done
				LOGGER.debug(DEFLoggerFactory.createTaskContext(program.getId(), job.getId(), tId), "Notify Job that Task changed state from {} to {}.", oldTaskState, task.getState());
				job.notifyTaskChangedState(tId, oldTaskState, task.getState());

				// Reduce
				if (job.hasReduceRoutine() && job.allTasksSuccessful()) {
					Future<List<ResourceDTO>> futureReduceResults = clusterResource
							.getReducerSchedulerServiceClient()
							.finalizeReduce(task.getJobId());
					List<ResourceDTO> reduceResults = futureReduceResults.get();
					job.setReducedResults(reduceResults);
					LOGGER.info(DEFLoggerFactory.createJobContext(job.getProgram().getId(), job.getId()), "Reduce done.");
				}

			} catch (UnknownNodeException | UnknownJobException | ExecutionException | InterruptedException | ClientCommunicationException e) {
				LOGGER.error(DEFLoggerFactory.createTaskContext(tId), "Error while fetching finished Task from Worker {}.", wId, e);
			} catch (IOException | UnknownCacheObjectException e) {
				LOGGER.error(DEFLoggerFactory.createTaskContext(tId), "Error while fetch task from cache.", e);
			}
		}
		LOGGER.debug("Notify Tasks successful done.");
	}

	/**
	 *
	 * @param taskIds
	 */
	void notifyTasksRun(List<String> taskIds) {
		LOGGER.debug("Notify Tasks run");
		for (String tId : taskIds) {
			try {
				TaskDTO task = taskCache.fetch(tId);
				LOGGER.debug(DEFLoggerFactory.createTaskContext(tId), "Notify Job that Task changed state.");
				programMap.get(task.getProgramId()).getJobById(task.getJobId()).notifyTaskChangedState(
						tId,
						task.getState(),
						ExecutionState.RUN
				);
				task.setState(ExecutionState.RUN);
				task.setStartTime(System.currentTimeMillis());
				taskCache.cache(task.getId(), task);
			} catch (IOException | UnknownJobException | UnknownCacheObjectException e) {
				LOGGER.error(DEFLoggerFactory.createTaskContext(tId), "Error while update Task to state {}.", ExecutionState.RUN, e);
			}
		}
	}

	/**
	 * Re-schedule the given tasks.
	 *
	 * @param taskIds - task (ids) to re-schedule
	 */
	void reSchedule(Collection<String> taskIds) {
		for (String taskId : taskIds) {
			LOGGER.debug(DEFLoggerFactory.createTaskContext(taskId), "Try to re-schedule Task.");
			try {
				TaskDTO task = taskCache.fetch(taskId);
				if (task != null) {
					try {
						Future<Void> futureScheduleTask = clusterResource
								.getSchedulerServiceClient(NodeType.WORKER)
								.scheduleTask(task.getJobId(), task);

						futureScheduleTask.get(); // wait for ticket to be done
						LOGGER.info(DEFLoggerFactory.createTaskContext(taskId), "Task re-scheduled successfully.");
					} catch (ClientCommunicationException | InterruptedException | ExecutionException e) {
						LOGGER.error(DEFLoggerFactory.createTaskContext(taskId), "Task re-scheduled failed.", e);
					}
				} else {
					LOGGER.warn("Task {} not known as active task.", taskId);
				}
			} catch (UnknownCacheObjectException | IOException e) {
				LOGGER.error(DEFLoggerFactory.createTaskContext(taskId), "Error while fetch Task from cache.", e);
			}
		}
	}

	private long calcRuntimeForTask(TaskDTO task) {
		switch (task.getState()) {
			case SCHEDULED:
				return 0;
			case RUN:
				return System.currentTimeMillis() - task.getStartTime();
			case SUCCESS:
			case FAILED:
				return task.getFinishTime() - task.getStartTime();
		}
		return -1;
	}
}
