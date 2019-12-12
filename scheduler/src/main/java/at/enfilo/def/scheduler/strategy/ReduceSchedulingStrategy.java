package at.enfilo.def.scheduler.strategy;

import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.NodeCommunicationException;
import at.enfilo.def.reducer.api.IReducerServiceClient;
import at.enfilo.def.reducer.api.ReducerServiceClientFactory;
import at.enfilo.def.scheduler.api.ScheduleReduceException;
import at.enfilo.def.scheduler.api.strategy.IReduceSchedulingStrategy;
import at.enfilo.def.scheduler.util.SchedulerConfiguration;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ResourceDTO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Abstract base class for every scheduler implementation.
 *
 */
public abstract class ReduceSchedulingStrategy extends SchedulingStrategy<IReducerServiceClient, ReducerServiceClientFactory>
implements IReduceSchedulingStrategy {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ReduceSchedulingStrategy.class);

	private final Map<String, String> jobs;
	private Map<String, List<String>> jobNodeMap = new HashMap<>();

	/**
	 * Constructor for implementation.
	 */
	protected ReduceSchedulingStrategy(SchedulerConfiguration schedulerConfiguration) {
		this(
				new ConcurrentHashMap<>(),
				new ConcurrentHashMap<>(),
				new ConcurrentHashMap<>(),
				new ReducerServiceClientFactory(),
				null,
				schedulerConfiguration
		);
	}

	/**
	 * Constructor for unit test - concrete Class TestScheduler.
	 *
	 * @param jobs     - internal job map.
	 * @param reducers - internal worker map.
	 */
	protected ReduceSchedulingStrategy(
			Map<String, String> jobs,
			Map<String, IReducerServiceClient> reducers,
			Map<String, Environment> reducerEnvironments,
			ReducerServiceClientFactory reducerServiceClientFactory,
			ILibraryServiceClient libraryServiceClient,
			SchedulerConfiguration schedulerConfiguration
	) {
		super(reducers, reducerEnvironments, reducerServiceClientFactory, libraryServiceClient, schedulerConfiguration);
		this.jobs = jobs;
	}

	/**
	 * Adds a new reduce job: Starts the reduce routine on each worker.
	 *
	 * @param jId - job id
	 * @param reduceRoutineId
	 */
	@Override
	public void addJob(String jId, String reduceRoutineId) throws ScheduleReduceException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Try to add ReduceJob with routine {}.", reduceRoutineId);
		if (!jobs.containsKey(jId)) {
			try {
				// Create reduce jobs on all matching reducer nodes
				List<String> mNodes = getNodes(Collections.singletonList(reduceRoutineId));

				List<Future<Void>> futures = new LinkedList<>();
				for (String reducerId : mNodes) {
					IReducerServiceClient client = getNodeClient(reducerId);
					Future<Void> createReduceJobFuture = client.createReduceJob(jId, reduceRoutineId);
					futures.add(createReduceJobFuture);
				}
				// Check if creation was successful
				for (Future<Void> future : futures) {
					future.get();
				}

				jobs.put(jId, reduceRoutineId);
				jobNodeMap.put(jId, mNodes);
				LOGGER.info(DEFLoggerFactory.createJobContext(jId), "ReduceJob with routine {} successfully added.", reduceRoutineId);

			} catch (ExecutionException | UnknownNodeException | ClientCommunicationException e) {
				LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while create ReduceJob {} on a Reducer.", jId, e);
				throw new ScheduleReduceException(e);
			} catch (InterruptedException e) {
				LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while create ReduceJob {} on a Reducer. Interrupted.", jId, e);
				Thread.currentThread().interrupt();
				throw new ScheduleReduceException(e);
			}


		} else {
			LOGGER.warn(DEFLoggerFactory.createJobContext(jId),"Reduce job already added, ignoring request.");
		}
	}

	/**
	 * Adds a new reducer to this scheduler.
	 *
	 * @param nId             - reducer node id
	 * @param serviceEndpoint - service endpoint
	 */
	@Override
	public void addReducer(String nId, ServiceEndpointDTO serviceEndpoint) throws NodeCommunicationException {
		addNode(nId, serviceEndpoint);
	}

	/**
	 * Finalize a reduce - this means all resources are added (see {@link IReduceSchedulingStrategy ::scheduleReduce})
	 *
	 * @param jId - reduce job id
	 * @return
	 */
	@Override
	public List<ResourceDTO> finalizeReduce(String jId) throws ScheduleReduceException {
		try {
			// Send a final reduce to all reducers.
			List<Future<Void>> futures = new LinkedList<>();
			for (String reducerId : jobNodeMap.get(jId)) {
				IReducerServiceClient client = getNodeClient(reducerId);
				Future<Void> reduceFuture = client.reduce(jId);
				futures.add(reduceFuture);
			}
			// Check if creation was successful
			for (Future<Void> future : futures) {
				future.get();
			}

			// Fetch all reduced results
			List<ResourceDTO> reducedResources = new LinkedList<>();
			for (String reducerId : jobNodeMap.get(jId)) {
				IReducerServiceClient client = getNodeClient(reducerId);
				Future<List<ResourceDTO>> resultFuture = client.fetchResult(jId);
				reducedResources.addAll(resultFuture.get());
			}

			// Remove job
			jobs.remove(jId);
			jobNodeMap.remove(jId);

			return reducedResources;

		} catch (ClientCommunicationException | UnknownNodeException | ExecutionException e) {
			String msg = String.format("Error while send final reduce for %s to reducers or while fetching results.", jId);
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), msg, e);
			throw new ScheduleReduceException(e);
		} catch (InterruptedException e) {
			String msg = String.format("Error while send final reduce for %s to reducers or while fetching results.", jId);
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), msg, e);
			Thread.currentThread().interrupt();
			throw new ScheduleReduceException(e);
		}
	}

	/**
	 * Removes a reducer node.
	 *
	 * @param nId - reducer node id.
	 */
	@Override
	public void removeReducer(String nId) {
		removeNode(nId);
	}

	/**
	 * Schedules a list of resources to available reducer nodes.
	 *
	 * @param jId       - job id
	 * @param resources - resource to scheduler or "reduce"
	 */
	@Override
	public void scheduleReduce(String jId, List<ResourceDTO> resources) throws ScheduleReduceException {
		List<String> reducers = jobNodeMap.get(jId);
		try {
			List<Future<Void>> futures = new LinkedList<>();
			for (ResourceDTO resource : resources) {
				String reducerId = map(resource.getKey(), reducers);
				IReducerServiceClient client = getNodeClient(reducerId);
				Future<Void> ticketStatusFuture = client.add(jId, Collections.singletonList(resource));
				futures.add(ticketStatusFuture);
			}

		} catch (ClientCommunicationException | UnknownNodeException e) {
			LOGGER.error("Error while schedule resources to reducers.", e);
			throw new ScheduleReduceException("Error while schedule resources to reducers.", e);
		}
	}

	protected abstract String map(String key, List<String> nodes);

	/**
	 * Notification about new task state.
	 *
	 * @param nId      - notification from worker node
	 * @param taskIds  - list of task id's
	 * @param newState - new state
	 */
	@Override
	public void notifyTasksNewState(String nId, List<String> taskIds, ExecutionState newState) {

	}

	/**
	 * Returns a list of active jobs
	 *
	 * @return
	 */
	@Override
	public List<String> getJobs() {
		return new LinkedList<>(jobs.keySet());
	}

	/**
	 * Deletes and aborts the given reduce job.
	 *
	 * @param jId - job id to abort/delete.
	 * @throws UnknownJobException
	 */
	@Override
	public void deleteJob(String jId) throws UnknownJobException, ScheduleReduceException {
		if (!jobs.containsKey(jId)) {
			String msg = String.format("Cannot delete an unknown Job %s.", jId);
			LOGGER.warn(DEFLoggerFactory.createJobContext(jId), msg);
			throw new UnknownJobException(msg);
		}
		try {
			// Delete reduce jobs on all reducer nodes
			List<Future<Void>> futures = new LinkedList<>();
			for (String reducerId : jobNodeMap.get(jId)) {
				IReducerServiceClient client = getNodeClient(reducerId);
				Future<Void> deleteReduceJobFuture = client.deleteReduceJob(jId);
				futures.add(deleteReduceJobFuture);
			}
			// Check if creation was successful
			for (Future<Void> future : futures) {
				future.get();
			}

			jobs.remove(jId);
			jobNodeMap.remove(jId);

		} catch (ClientCommunicationException | UnknownNodeException | ExecutionException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while delete ReduceJob {} on a Reducer.", jId, e);
			throw new ScheduleReduceException(e);
		} catch (InterruptedException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while delete ReduceJob {} on a Reducer. Interrupted.", jId, e);
			Thread.currentThread().interrupt();
			throw new ScheduleReduceException(e);
		}
	}
}
