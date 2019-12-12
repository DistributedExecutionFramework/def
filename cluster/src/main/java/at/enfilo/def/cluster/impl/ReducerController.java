package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.api.NodeCreationException;
import at.enfilo.def.cluster.api.NodeExecutionException;
import at.enfilo.def.cluster.api.UnknownNodeException;
import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.cluster.util.configuration.ReducersConfiguration;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.reducer.api.IReducerServiceClient;
import at.enfilo.def.reducer.api.ReducerServiceClientFactory;
import at.enfilo.def.scheduler.reducer.api.IReducerSchedulerServiceClient;
import at.enfilo.def.transfer.dto.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ReducerController extends NodeController<IReducerServiceClient, ReducerServiceClientFactory> {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ReducerController.class);

	private static ReducerController instance;

	private final Map<String, Set<String>> reducerJobAssignment;
	private final Map<String, Set<String>> jobKeyAssignment;
	private final IReducerSchedulerServiceClient reducerSchedulerServiceClient;

	private static final Object INSTANCE_LOCK = new Object();
	private final Object assignmentLock;

	public static ReducerController getInstance() {
		synchronized (INSTANCE_LOCK) {
			if (instance == null) {
				instance = new ReducerController();
			}
			return instance;
		}
	}

	private ReducerController() {
		this(
			new ReducerServiceClientFactory(),
			new LinkedList<>(),
			new HashMap<>(),
			new HashMap<>(),
			null, // null means a TimeOut Map will be created
			new HashMap<>(),
			Cluster.getInstance().getConfiguration().getReducersConfiguration(),
			new HashMap<>(),
			new HashMap<>()
		);
	}

	/**
	 * Constructor for internal/unit test usage.
	 * @param reducerServiceClientFactory
	 * @param nodes
	 * @param nodeInstanceMap
	 * @param nodeConnectionMap
	 * @param nodeInfoMap
	 * @param reducersConfiguration
	 */
	ReducerController(
			ReducerServiceClientFactory reducerServiceClientFactory,
			List<String> nodes,
			Map<String, String> nodeInstanceMap,
			Map<String, IReducerServiceClient> nodeConnectionMap,
			Map<String, NodeInfoDTO> nodeInfoMap,
			Map<String, List<FeatureDTO>> nodeFeatureMap,
			ReducersConfiguration reducersConfiguration,
			Map<String, Set<String>> reducerJobAssignment,
			Map<String, Set<String>> jobKeyAssignment
	) {
		super(
				NodeType.REDUCER,
				reducerServiceClientFactory,
				nodes,
				nodeInstanceMap,
				nodeConnectionMap,
				nodeInfoMap,
				nodeFeatureMap,
				reducersConfiguration,
				reducersConfiguration.getStoreRoutineId()
		);
		this.assignmentLock = new Object();
		this.reducerJobAssignment = reducerJobAssignment;
		this.jobKeyAssignment = jobKeyAssignment;
		this.reducerSchedulerServiceClient = ClusterResource.getInstance().getReducerSchedulerServiceClient();
	}

	public String addReducer(ServiceEndpointDTO serviceEndpoint) throws NodeCreationException {
		return super.addNode(serviceEndpoint);
	}

	public String getStoreRoutineId() {
		return super.getStoreRoutineId();
	}

	public void setStoreRoutineId(String storeRoutineId) {
		super.setStoreRoutineId(storeRoutineId);
	}

	public void distributeSharedResource(ResourceDTO sharedResource) {
		super.distributeSharedResource(sharedResource);
	}

	public void removeSharedResources(List<String> sharedResources) {
		super.removeSharedResources(sharedResources);
	}

	/**
	 * Setup reducerResourceAssignment map if needed.
	 *
	 * @param rId - for given reducer id
	 * @throws UnknownNodeException
	 */
	private void setupReducerResourceAssignment(String rId, String jId) throws UnknownNodeException {
		synchronized (nodeLock) {
			if (!nodeInfoMap.containsKey(rId)) {
				LOGGER.error(String.format(UNKNOWN_NODE, rId));
				throw new UnknownNodeException(String.format(UNKNOWN_NODE, rId));
			}
		}
		synchronized (assignmentLock) {
			if (!reducerJobAssignment.containsKey(rId)) {
				reducerJobAssignment.put(rId, new HashSet<>());
			}
			reducerJobAssignment.get(rId).add(jId);
			if (!jobKeyAssignment.containsKey(jId)) {
				jobKeyAssignment.put(jId, new HashSet<>());
			}
		}
	}

	/**
	 * Remove a node from this cluster. Notifies scheduler to remove the specified node.
	 * Re-schedule resources from removed reducer.
	 *
	 * @param nId - Node to remove
	 * @throws UnknownNodeException - if node id not known
	 */
	protected void removeNodeAssignments(String nId) throws UnknownNodeException {
		LOGGER.debug("Removing node {}.", nId);
		Map<String, Set<String>> jobKeysMap = new HashMap<>();
		synchronized (assignmentLock) {
			if (reducerJobAssignment.containsKey(nId)) {
				for (String jId :reducerJobAssignment.get(nId)) {
					Set<String> resourceKeys = new HashSet<>();
					if (jobKeyAssignment.containsKey(jId)) {
						resourceKeys.addAll(jobKeyAssignment.get(jId));
						jobKeysMap.put(jId, resourceKeys);
						jobKeyAssignment.remove(jId);
					}
				}
				reducerJobAssignment.remove(nId);
			}
		}

		// Re-schedule resources if needed
		for (Map.Entry<String, Set<String>> entry : jobKeysMap.entrySet()) {
			if (!entry.getValue().isEmpty()) {
				ClusterExecLogicController.getInstance().reScheduleReduceResources(entry.getKey(), entry.getValue());
			}
		}
	}

	public void removeNode(String nId) throws UnknownNodeException {
		super.removeNode(nId, true);
	}

	public void notifyJobsNewState(String nId, List<String> jobIds, ExecutionState newState) throws UnknownNodeException {
		LOGGER.debug("Notify jobs have new state " + newState + " from reducer with id " + nId);
		switch (newState) {
			case SUCCESS:
			case FAILED:
				for (String jId : jobIds) {
					setupReducerResourceAssignment(nId, jId);
					synchronized (assignmentLock) {
						jobKeyAssignment.remove(jId);
					}
				}
				synchronized (assignmentLock) {
					jobIds.forEach(reducerJobAssignment.get(nId)::remove);
				}
				break;
			case SCHEDULED:
			case RUN:
			default:
				// Ignoring this states
				break;
		}
	}

	public void notifyReduceKeysReceived(String nId, String jId, List<String> reduceKeys) throws UnknownNodeException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "{} reduce keys received from worker {}.", reduceKeys.size(), nId);
		setupReducerResourceAssignment(nId, jId);
		synchronized (assignmentLock) {
			reduceKeys.forEach(jobKeyAssignment.get(jId)::add);
		}
	}

	public void deleteReduceJob(String jId) throws NodeExecutionException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Aborting reduce job on reducers.");
		String rId = null;
		synchronized (assignmentLock) {
			for (Map.Entry<String, Set<String>> e: reducerJobAssignment.entrySet()) {
				if (e.getValue().contains(jId)) {
					rId = e.getKey();
					break;
				}
			}
		}

		if (rId != null) {
			try {
				Future<Void> future = reducerSchedulerServiceClient.removeReduceJob(jId);
				future.get(); // Wait for ticket.
			} catch (ExecutionException | ClientCommunicationException e) {
				LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while sending abort job to reducer {}.", rId, e);
				throw new NodeExecutionException(e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while sending abort job to reducer {}. Interrupted.", rId, e);
				throw new NodeExecutionException(e);
			}
		}
	}

	public void scheduleResourcesToReduce(String jId, List<ResourceDTO> resources) throws NodeExecutionException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Scheduling resources to reduce to reducers.");
		try {
			Future<Void> future = reducerSchedulerServiceClient.scheduleResourcesToReduce(jId, resources);
			future.get(); // Wait for ticket.
		} catch (ExecutionException | ClientCommunicationException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while scheduling resources to reduce.", e);
			throw new NodeExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while scheduling resources to reduce. Interrupted.", e);
			throw new NodeExecutionException(e);
		}
	}

	public JobDTO finalizeReduce(String jId) throws NodeExecutionException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(jId), "Finalizing reduce job on reducers.");
		try {
			Future<JobDTO> future = reducerSchedulerServiceClient.finalizeReduce(jId);
			return future.get();
		} catch (ClientCommunicationException | ExecutionException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while finalizing reduce.", e);
			throw new NodeExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error(DEFLoggerFactory.createJobContext(jId), "Error while finalizing reduce. Interrupted.", e);
			throw new NodeExecutionException(e);
		}
	}

	public void addReduceJob(JobDTO job) throws NodeExecutionException {
		LOGGER.debug(DEFLoggerFactory.createJobContext(job.getProgramId(), job.getId()), "Adding reduce job to reducers.");
		try {
			Future<Void> future = reducerSchedulerServiceClient.addReduceJob(job);
			future.get();
		} catch (ExecutionException | ClientCommunicationException e) {
			LOGGER.error(DEFLoggerFactory.createJobContext(job.getProgramId(), job.getId()), "Error while adding reduce job.", e);
			throw new NodeExecutionException(e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.error(DEFLoggerFactory.createJobContext(job.getProgramId(), job.getId()), "Error while adding reduce job. Interrupted.", e);
			throw new NodeExecutionException(e);
		}
	}
}
