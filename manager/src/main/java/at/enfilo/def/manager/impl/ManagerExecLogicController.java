package at.enfilo.def.manager.impl;

import at.enfilo.def.common.api.ITimeoutMap;
import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.impl.TimeoutMap;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.execlogic.impl.ExecLogicException;
import at.enfilo.def.execlogic.impl.IExecLogicController;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.ContextSetBuilder;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.manager.util.ProgramClusterRegistry;
import at.enfilo.def.persistence.api.IPersistenceFacade;
import at.enfilo.def.persistence.dao.PersistenceFacade;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.UnknownTaskException;
import at.enfilo.def.transfer.dto.*;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

// TODO: Discuss logic to update database: Program, Job, Task, etc.
// Currently there is just a delegate to a cluster service
public class ManagerExecLogicController implements IExecLogicController {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ManagerExecLogicController.class);

	private static final int CLUSTER_CLIENT_EXPIRATION = 60;
	private static final TimeUnit CLUSTER_CLIENT_EXPIRATION_UNIT = TimeUnit.MINUTES;

	/**
	 * Private class to provide thread safe singleton
	 */
	private static class ThreadSafeLazySingletonWrapper {
		private static final ManagerExecLogicController INSTANCE = new ManagerExecLogicController();

		private ThreadSafeLazySingletonWrapper() {
		}
	}

	private final ExecLogicServiceClientFactory execLogicServiceClientFactory;
	private final ProgramClusterRegistry registry;
	private final IPersistenceFacade persistenceFacade;
	private final ITimeoutMap<String, IExecLogicServiceClient> clusterClientCache;

	/**
	 * Singleton pattern.
	 *
	 * @return an instance of ExecutionController
	 */
	public static ManagerExecLogicController getInstance() {
		return ThreadSafeLazySingletonWrapper.INSTANCE;
	}


	/**
	 * Hide constructor: singleton pattern
	 */
	private ManagerExecLogicController() {
		this(
				new ExecLogicServiceClientFactory(),
				ProgramClusterRegistry.getInstance(),
				new PersistenceFacade()
		);
	}

	/**
	 * Private constructor for unit tests.
	 */
	private ManagerExecLogicController(
			ExecLogicServiceClientFactory execLogicServiceClientFactory,
			ProgramClusterRegistry registry,
			IPersistenceFacade persistenceFacade
	) {
		this.execLogicServiceClientFactory = execLogicServiceClientFactory;
		this.registry = registry;
		this.persistenceFacade = persistenceFacade;
		this.clusterClientCache = new TimeoutMap<>(
					CLUSTER_CLIENT_EXPIRATION,
					CLUSTER_CLIENT_EXPIRATION_UNIT,
					CLUSTER_CLIENT_EXPIRATION / 2,
					CLUSTER_CLIENT_EXPIRATION_UNIT
		);
	}


	private IExecLogicServiceClient getClusterClient(String cId) throws UnknownClusterException, ClientCreationException {
		if (!clusterClientCache.containsKey(cId)) {
			IExecLogicServiceClient client = execLogicServiceClientFactory.createClient(registry.getClusterEndpoint(cId));
			clusterClientCache.put(cId, client);
		}
		return clusterClientCache.get(cId);
	}


	/**
	 * Returns all programs stored by manager/cluster.
	 *
	 * @param userId
	 * @return list of program ids
	 * @throws Exception
	 */
	@Override
	public List<String> getAllPrograms(String userId) throws ExecLogicException {
//		LOGGER.debug("Try to fetch all programs belong to user {} from database", userId);
//		try {
//			// TODO
//			//List<Program> programs = persistenceFacade.getNewUserDAO().getAllPrograms(userId);
//			//LOGGER.debug("Found {} programs for User {}", programs.size(), userId);
//			//return programs.stream().map(Program::getId).collect(Collectors.toList());
//		} catch (PersistenceException e) {
//			LOGGER.error("Error while fetching Programs for User {} from database", userId, e);
//			throw new ExecLogicException(e);
//		}

		LOGGER.info("Try to fetch all programs that belong to user {} from the registry.", userId);

		if (userId.equalsIgnoreCase("defadmin")) {
			return registry.getProgramIds();
		}

		List<String> clusterIds = registry.getClusterIds();
		List<String> programIds = new LinkedList<>();

		for (String cId : clusterIds) {
			try {
				Future<List<String>> pIdsFuture = getClusterClient(cId).getAllPrograms(userId);
				programIds.addAll(pIdsFuture.get());
			} catch (UnknownClusterException
					| ClientCreationException
					| ClientCommunicationException
					| InterruptedException
					| ExecutionException e) {
				LOGGER.error("Error while fetching all programs of user {} from registry.", userId);
			}
		}

		return programIds;
	}

	@Override
	public String createProgram(String cId, String uId) throws ExecLogicException {
		LOGGER.debug("Delegate create Program to Cluster {}.", cId);
		try {
			Future<String> futurePId = getClusterClient(cId).createProgram(cId, uId);
			String pId = futurePId.get();
			registry.bindProgramToCluster(pId, cId);
			LOGGER.info(createLoggingContext(pId), "Created Program on Cluster {} successful.", cId);
			return pId;
		} catch (ExecutionException | ClientCommunicationException | InterruptedException |
				ClientCreationException | UnknownClusterException e) {
			LOGGER.error("Error while delegate create Program to Cluster {}.", cId, e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public ProgramDTO getProgram(String pId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate get Program to Cluster {}.", cId);
			Future<ProgramDTO> futureProgram = getClusterClient(cId).getProgram(pId);
			ProgramDTO program = futureProgram.get();
			LOGGER.debug(indicators, "Fetched Program from Cluster {} successful.", cId);
			return program;

		} catch (ExecutionException | ClientCommunicationException |
				ClientCreationException | InterruptedException | UnknownClusterException e) {
			LOGGER.error(indicators, "Error while delegate getProgram() to Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void deleteProgram(String pId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate delete Program to Cluster {}.", cId);
			Future<Void> futureDeleteProgram = getClusterClient(cId).deleteProgram(pId);
			futureDeleteProgram.get();
			registry.unbindProgram(pId);
			LOGGER.info(indicators, "Delete program finished successful on Cluster {}.", cId);

		} catch (ExecutionException | ClientCommunicationException |
				InterruptedException | ClientCreationException | UnknownClusterException e) {

			LOGGER.error(indicators, "Error delete program on Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void abortProgram(String pId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate abort Program to Cluster {}.", cId);
			Future<Void> futureAbortProgram = getClusterClient(cId).abortProgram(pId);
			futureAbortProgram.get();
			LOGGER.info(indicators, "Aborting program finished successfully on Cluster {}.", cId);

		} catch (ExecutionException | ClientCommunicationException |
				InterruptedException | ClientCreationException | UnknownClusterException e) {
			LOGGER.error(indicators, "Error aborting program on Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void updateProgramName(String pId, String name) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate update program name to Cluster {}.", cId);
			Future<Void> futureUpdateProgramName = getClusterClient(cId).updateProgramName(pId, name);
			futureUpdateProgramName.get();
			LOGGER.info(indicators, "Updating name of program finished successfully on Cluster {}.", cId);
		} catch (ExecutionException | ClientCommunicationException |
				InterruptedException | ClientCreationException | UnknownClusterException e) {
			LOGGER.error(indicators, "Error updating program on Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void updateProgramDescription(String pId, String description) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate update program description to Cluster {}.", cId);
			Future<Void> future = getClusterClient(cId).updateProgramDescription(pId, description);
			future.get();
			LOGGER.info(indicators, "Updating description of program finished successfully on Cluster {}.", cId);
		} catch (ExecutionException | ClientCommunicationException |
				InterruptedException | ClientCreationException | UnknownClusterException e) {
			LOGGER.error(indicators, "Error updating program on Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void startClientRoutine(String pId, String crId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId);

		if (!registry.isProgramRegistered(pId)) {
			// TODO: Try to fetch from Database
			String msg = "Program not registered.";
			LOGGER.error(indicators, msg);
			throw new UnknownProgramException(msg);
		}

		try {
			String cId = registry.getClusterId(pId);
			LOGGER.debug(indicators, "Delegate attach and start client routine to Cluster {}.", cId);
			Future<Void> future = getClusterClient(cId).startClientRoutine(pId, crId);
			future.get(); // Wait for ticket.
			LOGGER.info(indicators, "Attaching and starting client routine successfully finished on cluster {}.", cId);
		} catch (ExecutionException | ClientCommunicationException | UnknownProgramException |
				InterruptedException | ClientCreationException | UnknownClusterException e) {
			LOGGER.error(indicators, "Error attaching and starting client routine on cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void markProgramAsFinished(String pId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate mark Program as finished to Cluster {}.", cId);
			Future<Void> futureMarkFinished = getClusterClient(cId).markProgramAsFinished(pId);
			futureMarkFinished.get();
			LOGGER.info(indicators, "Marked Program as finished successful on Cluster {}.", cId);

		} catch (ExecutionException | ClientCommunicationException | InterruptedException |
				ClientCreationException | UnknownClusterException e) {

			LOGGER.error(indicators, "Error mark Program as finished on Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public List<String> getAllJobs(String pId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate get all Jobs to Cluster {}.", cId);
			Future<List<String>> futureAllJobs = getClusterClient(cId).getAllJobs(pId);
			List<String> jobs = futureAllJobs.get();
			LOGGER.debug(indicators, "Fetched all Jobs from Cluster {} successful.", cId);
			return jobs;

		} catch (ExecutionException | ClientCommunicationException | InterruptedException |
				UnknownClusterException | ClientCreationException e) {

			LOGGER.error(indicators, "Error fetch all Jobs from Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public String createJob(String pId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate create Job to Cluster {}.", cId);
			Future<String> futureCreateJob = getClusterClient(cId).createJob(pId);
			String jId = futureCreateJob.get();
			indicators = createLoggingContext(pId, jId);
			LOGGER.info(indicators, "Created Job successful on Cluster {}.", cId);
			return jId;

		} catch (ExecutionException | ClientCommunicationException |
				InterruptedException | UnknownClusterException | ClientCreationException e) {

			LOGGER.error(indicators, "Error create Job on Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public JobDTO getJob(String pId, String jId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate get Job to Cluster {}.", cId);
			Future<JobDTO> futureJob = getClusterClient(cId).getJob(pId, jId);
			JobDTO job = futureJob.get();
			LOGGER.debug(indicators, "Fetched Job from Cluster {} successful.", cId);
			return job;

		} catch (ExecutionException | ClientCommunicationException |
				ClientCreationException | InterruptedException | UnknownClusterException e) {

			LOGGER.error(indicators, "Error fetch Job from Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void deleteJob(String pId, String jId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate delete Job to Cluster {}.", cId);
			Future<Void> futureDeleteJob = getClusterClient(cId).deleteJob(pId, jId);
			futureDeleteJob.get();
			LOGGER.info(indicators, "Deleted Job from Cluster {} successful.", cId);
		} catch (ExecutionException | ClientCommunicationException |
				ClientCreationException | UnknownClusterException | InterruptedException e) {

			LOGGER.error(indicators, "Error Deleting job from Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public String getAttachedMapRoutine(String pId, String jId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate get AttachedMapRoutine to Cluster {}.", cId);
			Future<String> futureRoutine = getClusterClient(cId).getAttachedMapRoutine(pId, jId);
			String routineId = futureRoutine.get();
			LOGGER.debug(indicators, "Fetched attached MapRoutine {} from Cluster {} successful.", routineId, cId);
			return routineId;

		} catch (ExecutionException | UnknownClusterException | ClientCreationException |
				 InterruptedException | ClientCommunicationException e) {

			LOGGER.error(indicators, "Error fetching attached MapRoutine from Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void attachMapRoutine(String pId, String jId, String mapRoutineId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate attach MapRoutine {} to Cluster {}.", mapRoutineId, cId);
			Future<Void> futureMapRoutine = getClusterClient(cId).attachMapRoutine(pId, jId, mapRoutineId);
			futureMapRoutine.get();
			LOGGER.info(indicators, "Attached MapRoutine {} on Cluster {} successful.", mapRoutineId, cId);
		} catch (ExecutionException | ClientCommunicationException |
				InterruptedException | UnknownClusterException | ClientCreationException e) {

			LOGGER.error(indicators, "Error attaching MapRoutine {} on Cluster.", mapRoutineId, e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public String getAttachedReduceRoutine(String pId, String jId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate get AttachedReduceRoutine to Cluster {}.", cId);
			Future<String> futureRoutine = getClusterClient(cId).getAttachedReduceRoutine(pId, jId);
			String routineId = futureRoutine.get();
			LOGGER.debug(indicators, "Fetched attached ReduceRoutine {} from Cluster {} successful.", cId);
			return routineId;

		} catch (ExecutionException | UnknownClusterException | ClientCreationException |
				 InterruptedException | ClientCommunicationException e) {

			LOGGER.error(indicators, "Error fetching attached ReduceRoutine from Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void attachReduceRoutine(String pId, String jId, String reduceRoutineId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate attach ReduceRoutine {} to Cluster {}.", reduceRoutineId, cId);
			Future<Void> futureReduceRoutine = getClusterClient(cId).attachReduceRoutine(pId, jId, reduceRoutineId);
			futureReduceRoutine.get();
			LOGGER.info(indicators, "Attached ReduceRoutine {} on Cluster {} successful.", cId, reduceRoutineId);

		} catch (ExecutionException | ClientCommunicationException |
				InterruptedException | UnknownClusterException | ClientCreationException e) {

			LOGGER.error(indicators, "Error attaching ReduceRoutine {} on Cluster.", reduceRoutineId, e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public List<String> getAllTasks(String pId, String jId, SortingCriterion sortingCriterion) throws ExecLogicException, UnknownProgramException, UnknownJobException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate getAllTasks to Cluster {}.", cId);
			Future<List<String>> futureAllTasks = getClusterClient(cId).getAllTasks(pId, jId, sortingCriterion);
			List<String> taskIds = futureAllTasks.get();
			LOGGER.debug(indicators, "Fetched Task Ids from Cluster {} successfully.", cId);
			return taskIds;
		} catch (ExecutionException | ClientCommunicationException |
				ClientCreationException | UnknownClusterException | InterruptedException e) {

			LOGGER.error(indicators, "Error fetching all Task Ids from Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public List<String> getAllTasksWithState(String pId, String jId, ExecutionState state, SortingCriterion sortingCriterion) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate getAllTasksWithState to Cluster {}.", cId);
			Future<List<String>> futureAllTasks = getClusterClient(cId).getAllTasksWithState(pId, jId, state, sortingCriterion);
			List<String> taskIds = futureAllTasks.get();
			LOGGER.debug(indicators, "Fetched Task Ids from Cluster {} successful.", cId);
			return taskIds;
		} catch (ExecutionException | ClientCommunicationException |
				ClientCreationException | UnknownClusterException | InterruptedException e) {

			LOGGER.error(indicators, "Error fetching Task Ids with state from Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public String createTask(String pId, String jId, RoutineInstanceDTO objectiveRoutine)
			throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate create Task to Cluster {}.", cId);
			Future<String> futureCreateTask = getClusterClient(cId).createTask(pId, jId, objectiveRoutine);
			String tId = futureCreateTask.get();
			indicators = createLoggingContext(pId, jId, tId);
			LOGGER.info(indicators, "Created Task successful on Cluster {} successful.", cId);
			return tId;
		} catch (ExecutionException | ClientCommunicationException |
				ClientCreationException | UnknownClusterException | InterruptedException e) {

			LOGGER.error(indicators, "Error creating Task on Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public TaskDTO getTask(String pId, String jId, String tId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId, tId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate get Task to Cluster {}.", cId);
			Future<TaskDTO> futureTask = getClusterClient(cId).getTask(pId, jId, tId);
			TaskDTO task = futureTask.get();
			LOGGER.debug(indicators, "Fetched Task from Cluster {} successful.", cId);
			return task;

		} catch (ExecutionException | ClientCommunicationException |
				ClientCreationException | UnknownClusterException | InterruptedException e) {

			LOGGER.error(indicators, "Error getting Task from Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public TaskDTO getTaskPartial(String pId, String jId, String tId, boolean includeInParameters, boolean includeOutParameters)
	throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId, tId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate get Task to Cluster {}.", cId);
			Future<TaskDTO> futureTask = getClusterClient(cId).getTask(pId, jId, tId, includeInParameters, includeOutParameters);
			TaskDTO task = futureTask.get();
			LOGGER.debug(indicators, "Fetched Task from Cluster {} successful.", cId);
			return task;

		} catch (ExecutionException | ClientCommunicationException |
				ClientCreationException | UnknownClusterException | InterruptedException e) {

			LOGGER.error(indicators, "Error getting Task from Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void markJobAsComplete(String pId, String jId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate mark Job as complete to Cluster {}.", cId);
			Future<Void> futureJobComplete = getClusterClient(cId).markJobAsComplete(pId, jId);
			futureJobComplete.get();
			LOGGER.info(indicators, "Marked Job as complete successful on Cluster {}.", cId);

		} catch (ClientCreationException | ClientCommunicationException |
				InterruptedException | ExecutionException | UnknownClusterException e) {

			LOGGER.error(indicators, "Failed to mark Job as complete on Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void abortJob(String pId, String jId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate abort Job to Cluster {}.", cId);
			Future<Void> futureAbortJob = getClusterClient(cId).abortJob(pId, jId);
			futureAbortJob.get();
			LOGGER.info(indicators, "Abort Job on Cluster {} successful.", cId);

		} catch (ExecutionException | ClientCommunicationException |
				ClientCreationException | UnknownClusterException | InterruptedException e) {

			LOGGER.error(indicators, "Error abort Job on Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void abortTask(String pId, String jId, String tId)
	throws UnknownProgramException, UnknownJobException, UnknownTaskException, ExecLogicException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId, tId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate abort Task to Cluster {}.", cId);
			Future<Void> futureAbortTask = getClusterClient(cId).abortTask(pId, jId, tId);
			futureAbortTask.get();
			LOGGER.info(indicators, "Abort Task on Cluster {} successful.", cId);

		} catch (ExecutionException | ClientCommunicationException |
				ClientCreationException | UnknownClusterException | InterruptedException e) {

			LOGGER.error(indicators, "Error abort Task on Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void reRunTask(String pId, String jId, String tId) throws UnknownProgramException, ExecLogicException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId, jId, tId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate re-run Task to Cluster {}.", cId);
			Future<Void> futureReRunTask = getClusterClient(cId).reRunTask(pId, jId, tId);
			futureReRunTask.get();
			LOGGER.info(indicators, "Re-run Task on Cluster {} successful.", cId);

		} catch (ExecutionException | ClientCommunicationException |
				ClientCreationException | UnknownClusterException | InterruptedException e) {

			LOGGER.error(indicators, "Error re-run Task on Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public List<String> getAllSharedResources(String pId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate get all SharedResources to Cluster {}.", cId);
			Future<List<String>> futureSharedResources = getClusterClient(cId).getAllSharedResources(pId);
			List<String> sharedResources = futureSharedResources.get();
			LOGGER.debug(indicators, "Fetched SharedResources from Cluster {} successful.", cId);
			return sharedResources;

		} catch (ExecutionException | ClientCommunicationException | UnknownClusterException | ClientCreationException | InterruptedException e) {

			LOGGER.error(indicators, "Error getting all SharedResources from Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public String createSharedResource(String pId, String dataTypeId, ByteBuffer data) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug("Delegate create SharedResource to Cluster {}.", cId);
			Future<String> futureSharedResource = getClusterClient(cId).createSharedResource(pId, dataTypeId, data);
			String rId = futureSharedResource.get();
			LOGGER.info("Created SharedResource {} on Cluster {} successfully.", rId, cId);
			return rId;

		} catch (ExecutionException | ClientCommunicationException |
				InterruptedException | UnknownClusterException | ClientCreationException e) {

			LOGGER.error(indicators, "Error create SharedResource on Cluster.", e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public ResourceDTO getSharedResource(String pId, String rId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate get SharedResource {} to Cluster {}.", rId, cId);
			Future<ResourceDTO> futureResource = getClusterClient(cId).getSharedResource(pId, rId);
			ResourceDTO resource = futureResource.get();
			LOGGER.debug(indicators, "Fetched Program {} from Cluster {} successful.", pId, cId);
			return resource;
		} catch (ExecutionException | ClientCommunicationException |
				InterruptedException | ClientCreationException | UnknownClusterException e) {

			LOGGER.error(indicators, "Error getting SharedResource {} from Cluster..", rId, e);
			throw new ExecLogicException(e);
		}
	}

	@Override
	public void deleteSharedResource(String pId, String rId) throws ExecLogicException, UnknownProgramException {
		Set<ITuple<ContextIndicator, ?>> indicators = createLoggingContext(pId);

		try {
			String cId = fetchClusterForProgram(pId);
			LOGGER.debug(indicators, "Delegate delete SharedResource {} to Cluster {}.", rId, cId);
			Future<Void> futureDeleteResource = getClusterClient(cId).deleteSharedResource(pId, rId);
			futureDeleteResource.get();
			LOGGER.info(indicators, "Deleted SharedResource {} from Cluster {} successful.", rId, cId);
		} catch (ExecutionException | ClientCommunicationException |
				ClientCreationException | UnknownClusterException | InterruptedException e) {

			LOGGER.error(indicators, "Error deleting SharedResource {} on Cluster.", rId, e);
			throw new ExecLogicException(e);
		}
	}

	private Set<ITuple<ContextIndicator, ?>> createLoggingContext(String pId) {
		return createLoggingContext(pId, null, null);
	}

	private Set<ITuple<ContextIndicator, ?>> createLoggingContext(String pId, String jId) {
		return createLoggingContext(pId, jId, null);
	}

	private Set<ITuple<ContextIndicator, ?>> createLoggingContext(String pId, String jId, String tId) {
		ContextSetBuilder builder = new ContextSetBuilder();
		if (pId != null) {
			builder.add(ContextIndicator.PROGRAM_CONTEXT, pId);
		}
		if (jId != null) {
			builder.add(ContextIndicator.JOB_CONTEXT, jId);
		}
		if (tId != null) {
			builder.add(ContextIndicator.TASK_CONTEXT, tId);
		}
		return builder.build();
	}

	private String fetchClusterForProgram(String pId) throws UnknownProgramException {
		if (!registry.isProgramRegistered(pId)) {
			// TODO: try to fetch from Database
			String msg = "Program not registered.";
			LOGGER.error(DEFLoggerFactory.createProgramContext(pId), msg);
			throw new UnknownProgramException(msg);
		}
		return registry.getClusterId(pId);
	}
}
