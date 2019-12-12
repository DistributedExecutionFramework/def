package at.enfilo.def.cluster.impl;

import at.enfilo.def.cluster.server.Cluster;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.communication.exception.TakeControlException;
import at.enfilo.def.scheduler.api.ISchedulerServiceClient;
import at.enfilo.def.scheduler.api.SchedulerServiceClientFactory;
import at.enfilo.def.transfer.dto.CloudType;
import at.enfilo.def.transfer.dto.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class ClusterResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClusterResource.class);

	/**
	 * Thread safe singleton
	 */
	private static class ThreadSafeLazySingletonWrapper {
		private static final ClusterResource INSTANCE = new ClusterResource();

		private ThreadSafeLazySingletonWrapper() {}
	}

	private final String id;
	private final Instant startTime;
	private String managerId;
	private String name;
	private CloudType cloudType;
	private ISchedulerServiceClient reducerSchedulerService;
	private ISchedulerServiceClient workerSchedulerService;
	private String defaultMapRoutineId;


	/**
	 * Private constructor for avoid instances
	 */
	private ClusterResource() {
		this.id = Cluster.getInstance().getConfiguration().getId();
		this.startTime = Instant.now();

		try {
			this.reducerSchedulerService = new SchedulerServiceClientFactory().createClient(
				Cluster.getInstance().getConfiguration().getReducerSchedulerEndpoint()
			);

			this.workerSchedulerService = new SchedulerServiceClientFactory().createClient(
				Cluster.getInstance().getConfiguration().getWorkerSchedulerEndpoint()
			);
		} catch (ClientCreationException e) {
			LOGGER.error("Error while creating scheduler clients", e);
		}

		this.defaultMapRoutineId = Cluster.getInstance().getConfiguration().getDefaultMapRoutineId();
	}


	/**
	 * Singleton - get an instance.
	 * @return ClusterResource instance
	 */
	public static ClusterResource getInstance() {
		return ThreadSafeLazySingletonWrapper.INSTANCE;
	}


	public synchronized void takeControl(String managerId) throws TakeControlException {
		LOGGER.info("Take control request from Manager with id {}", managerId);
		if ((this.managerId == null) || this.managerId.equals(managerId)) {
			this.managerId = managerId;

		} else {
			String msg = "Cluster is already under control of Manager " + this.managerId;
			LOGGER.error(msg);
			throw new TakeControlException(msg);
		}
	}


	public String getId() {
		return id;
	}

	public String getManagerId() {
		return managerId;
	}

	public String getName() {
		if ((name == null) || name.isEmpty()) {
			return id;
		}
		return name;
	}

	public Instant getStartTime() {
		return startTime;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CloudType getCloudType() {
		return cloudType;
	}

	public void setCloudType(CloudType cloudType) {
		this.cloudType = cloudType;
	}

	public ISchedulerServiceClient getReducerSchedulerServiceClient() {
		return reducerSchedulerService;
	}

	public void setReducerSchedulerServiceClient(ISchedulerServiceClient reducerSchedulerService) {
		this.reducerSchedulerService = reducerSchedulerService;
	}

	public ISchedulerServiceClient getWorkerSchedulerServiceClient() {
		return workerSchedulerService;
	}

	public void setWorkerSchedulerServiceClient(ISchedulerServiceClient workerSchedulerService) {
		this.workerSchedulerService = workerSchedulerService;
	}

	public ISchedulerServiceClient getSchedulerServiceClient(NodeType nodeType)
	throws IllegalArgumentException {
		switch (nodeType) {
			case REDUCER: {
				return reducerSchedulerService;
			}

			case WORKER: {
				return workerSchedulerService;
			}

			default: throw new IllegalArgumentException("Can't return scheduler client for unknown ClusterNodeType.");
		}
	}

	public void setSchedulerService(NodeType nodeType, ServiceEndpointDTO endpoint)
	throws ClientCreationException {
		LOGGER.info("Set new SchedulerService of type {} at {}", nodeType, endpoint);

		switch (nodeType) {
			case REDUCER: {
				this.reducerSchedulerService = new SchedulerServiceClientFactory().createClient(endpoint);
				break;
			}
			case WORKER: {
				this.workerSchedulerService = new SchedulerServiceClientFactory().createClient(endpoint);
				break;
			}
			default: throw new ClientCreationException(new IllegalArgumentException(
				"Can't init/prepare scheduler client for unknown ClusterNodeType."
			));
		}
	}

	public String getDefaultMapRoutineId() {
		return defaultMapRoutineId;
	}

	public void setDefaultMapRoutineId(String defaultMapRoutineId) {
		this.defaultMapRoutineId = defaultMapRoutineId;
	}
}
