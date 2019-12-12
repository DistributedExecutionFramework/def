package at.enfilo.def.cluster.util.configuration;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.config.server.core.DEFRootConfiguration;
import at.enfilo.def.transfer.dto.CloudType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Created by mase on 09.09.2016.
 */
public class ClusterConfiguration extends DEFRootConfiguration {

	public static final String PROPERTY_CLOUD_TYPE = "cloud-type";
	public static final String PROPERTY_REDUCER_SCHEDULER_ENDPOINT = "reducer-scheduler-endpoint";
	public static final String PROPERTY_REDUCERS_CONFIGURATION = "reducers-configuration";
	public static final String PROPERTY_WORKER_SCHEDULER_ENDPOINT = "worker-scheduler-endpoint";
	public static final String PROPERTY_WORKERS_CONFIGURATION = "workers-configuration";
	public static final String PROPERTY_CLIENT_ROUTINE_WORKERS_SCHEDULER_ENDPOINT = "client-routine-workers-scheduler-endpoint";
	public static final String PROPERTY_CLIENT_ROUTINE_WORKERS_CONFIGURATION = "client-routine-workers-configuration";
	public static final String PROPERTY_LIBRARY_ENDPOINT = "library-endpoint";
	public static final String PROPERTY_DEFAULT_MAP_ROUTINE = "default-map-routine";
	public static final String PROPERTY_RESOURCE_SPACE = "resource-space";

	public static final String SCHEDULER_DEFAULT_HOST = "127.0.0.1";
	public static final int SCHEDULER_DEFAULT_PORT = 40022;
	public static final Protocol SCHEDULER_DEFAULT_PROTOCOL = Protocol.THRIFT_TCP;

	public static final String LIBRARY_DEFAULT_HOST = "127.0.0.1";
	public static final int LIBRARY_DEFAULT_PORT = 40042;
	public static final Protocol LIBRARY_DEFAULT_PROTOCOL = Protocol.THRIFT_TCP;

	public static final String DEFAULT_RESOURCE_SPACE = "file:/tmp/def";

    private CloudType cloudType = CloudType.PRIVATE;
	private ServiceEndpointDTO reducerSchedulerEndpoint = new ServiceEndpointDTO(
		SCHEDULER_DEFAULT_HOST,
		SCHEDULER_DEFAULT_PORT,
		SCHEDULER_DEFAULT_PROTOCOL
	);
	private ReducersConfiguration reducersConfiguration = ReducersConfiguration.getDefault();
	private ServiceEndpointDTO workerSchedulerEndpoint = new ServiceEndpointDTO(
		SCHEDULER_DEFAULT_HOST,
		SCHEDULER_DEFAULT_PORT,
		SCHEDULER_DEFAULT_PROTOCOL
	);
	private WorkersConfiguration workersConfiguration = WorkersConfiguration.getDefault();
	private ServiceEndpointDTO libraryEndpoint = new ServiceEndpointDTO(
		LIBRARY_DEFAULT_HOST,
		LIBRARY_DEFAULT_PORT,
		LIBRARY_DEFAULT_PROTOCOL
	);
	private ClientRoutineWorkersConfiguration clientRoutineWorkersConfiguration = ClientRoutineWorkersConfiguration.getDefault();
	private ServiceEndpointDTO clientRoutineWorkerSchedulerEndpoint = new ServiceEndpointDTO(
			SCHEDULER_DEFAULT_HOST,
			SCHEDULER_DEFAULT_PORT,
			SCHEDULER_DEFAULT_PROTOCOL
	);
	private String defaultMapRoutineId = "a923fd6a-521b-30ac-86a6-7e7adea90f5e";
	private String resourceSpace = DEFAULT_RESOURCE_SPACE;

	@JsonProperty(PROPERTY_CLOUD_TYPE)
    public CloudType getCloudType() {
        return cloudType;
    }

    @JsonProperty(PROPERTY_CLOUD_TYPE)
    public void setCloudType(CloudType cloudType) {
        this.cloudType = cloudType;
    }

	@JsonProperty(PROPERTY_REDUCERS_CONFIGURATION)
	public ReducersConfiguration getReducersConfiguration() {
		return reducersConfiguration;
	}

	@JsonProperty(PROPERTY_REDUCERS_CONFIGURATION)
	public void setReducersConfiguration(ReducersConfiguration reducersConfiguration) {
		this.reducersConfiguration = reducersConfiguration;
	}

    @JsonProperty(PROPERTY_WORKERS_CONFIGURATION)
	public WorkersConfiguration getWorkersConfiguration() {
		return workersConfiguration;
	}

	@JsonProperty(PROPERTY_WORKERS_CONFIGURATION)
	public void setWorkersConfiguration(WorkersConfiguration workersConfiguration) {
		this.workersConfiguration = workersConfiguration;
	}

	@JsonProperty(PROPERTY_CLIENT_ROUTINE_WORKERS_CONFIGURATION)
	public ClientRoutineWorkersConfiguration getClientRoutineWorkersConfiguration() { return clientRoutineWorkersConfiguration; }

	@JsonProperty(PROPERTY_CLIENT_ROUTINE_WORKERS_CONFIGURATION)
	public void setClientRoutineWorkersConfiguration(ClientRoutineWorkersConfiguration clientRoutineWorkersConfiguration) {
		this.clientRoutineWorkersConfiguration = clientRoutineWorkersConfiguration;
	}

	@JsonProperty(PROPERTY_REDUCER_SCHEDULER_ENDPOINT)
	public ServiceEndpointDTO getReducerSchedulerEndpoint() {
		return reducerSchedulerEndpoint;
	}

	@JsonProperty(PROPERTY_REDUCER_SCHEDULER_ENDPOINT)
	public void setReducerSchedulerEndpoint(ServiceEndpointDTO reducerSchedulerEndpoint) {
		this.reducerSchedulerEndpoint = reducerSchedulerEndpoint;
	}

	@JsonProperty(PROPERTY_WORKER_SCHEDULER_ENDPOINT)
	public ServiceEndpointDTO getWorkerSchedulerEndpoint() {
		return workerSchedulerEndpoint;
	}

	@JsonProperty(PROPERTY_WORKER_SCHEDULER_ENDPOINT)
	public void setWorkerSchedulerEndpoint(ServiceEndpointDTO workerSchedulerEndpoint) {
		this.workerSchedulerEndpoint = workerSchedulerEndpoint;
	}

	@JsonProperty(PROPERTY_CLIENT_ROUTINE_WORKERS_SCHEDULER_ENDPOINT)
	public ServiceEndpointDTO getClientRoutineWorkerSchedulerEndpoint() { return clientRoutineWorkerSchedulerEndpoint; }

	@JsonProperty(PROPERTY_CLIENT_ROUTINE_WORKERS_SCHEDULER_ENDPOINT)
	public void setPropertyClientRoutineWorkersSchedulerEndpoint(ServiceEndpointDTO clientRoutineWorkerSchedulerEndpoint) {
		this.clientRoutineWorkerSchedulerEndpoint = clientRoutineWorkerSchedulerEndpoint;
	}

	@JsonProperty(PROPERTY_LIBRARY_ENDPOINT)
	public ServiceEndpointDTO getLibraryEndpoint() {
		return libraryEndpoint;
	}

	@JsonProperty(PROPERTY_LIBRARY_ENDPOINT)
	public void setLibraryEndpoint(ServiceEndpointDTO libraryEndpoint) {
		this.libraryEndpoint = libraryEndpoint;
	}

	@JsonProperty(PROPERTY_DEFAULT_MAP_ROUTINE)
	public String getDefaultMapRoutineId() {
		return defaultMapRoutineId;
	}

	@JsonProperty(PROPERTY_DEFAULT_MAP_ROUTINE)
	public void setDefaultMapRoutineId(String defaultMapRoutineId) {
		this.defaultMapRoutineId = defaultMapRoutineId;
	}

	@JsonProperty(PROPERTY_RESOURCE_SPACE)
	public String getResourceSpace() {
		return resourceSpace;
	}

	@JsonProperty(PROPERTY_RESOURCE_SPACE)
	public void setResourceSpace(String resourceSpace) {
		this.resourceSpace = resourceSpace;
	}

	@JsonIgnore
	public static ClusterConfiguration getDefault() {
		return new ClusterConfiguration();
	}
}
