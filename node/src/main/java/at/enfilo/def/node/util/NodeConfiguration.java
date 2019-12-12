package at.enfilo.def.node.util;

import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.common.util.environment.domain.Feature;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.config.server.core.DEFRootConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by mase on 28.07.2017.
 */
public abstract class NodeConfiguration extends DEFRootConfiguration {

	private static final String PROPERTY_WORKING_DIR = "working-dir";
	private static final String PROPERTY_EXEC_THREADS = "exec-threads";
	private static final String PROPERTY_CLEANUP_WORKING_DIR_ON_START = "cleanup-working-dir";
	private static final String PROPERTY_MAX_TASK_EXEC_TIME = "max-task-exec-time";
	private static final String PROPERTY_MAX_TASK_EXEC_UNIT = "max-task-exec-unit";
	private static final String PROPERTY_LIBRARY_ENDPOINT = "library-endpoint";
	private static final String PROPERTY_CLUSTER_REGISTRATION = "cluster-registration";
	private static final String PROPERTY_CLUSTER_ENDPOINT = "cluster-endpoint";
	private static final String PROPERTY_MATLAB_RUNTIME = "matlab-runtime";
	private static final String PROPERTY_ENVIRONMENT = "environment";

	private static final String PRIMARY_FEATURE_GROUP = "language";

	private String workingDir = "/tmp/def/";
	private boolean cleanupWorkingDirOnStart = false;
	private int executionThreads = 4;
	private long maxTaskExecTime = 1;
	private TimeUnit maxTaskExecUnit = TimeUnit.DAYS;
	private ServiceEndpointDTO libraryEndpoint = new ServiceEndpointDTO(
		"localhost",
		40042,
		Protocol.THRIFT_TCP
	);
	private boolean clusterRegistration = false;
	private ServiceEndpointDTO clusterEndpoint = null;
	private String matlabRuntime = "/usr/local/MATLAB/MATLAB_Runtime/v94";
	private List<NodeFeatureConfiguration> environment = new ArrayList<>();

	/**
	 * Returns the working-dir-specific section of the configuration file.
	 *
	 * @return working-dir-specific configuration parameters.
	 */
	@JsonProperty(PROPERTY_WORKING_DIR)
	public String getWorkingDir() {
		return workingDir;
	}

	/**
	 * Sets the working-dir-specific section of the configuration file.
	 */
	@JsonProperty(PROPERTY_WORKING_DIR)
	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	/**
	 * Returns the cleanup-working-dir-on-start-specific section of the configuration file.
	 *
	 * @return cleanup-working-dir-on-start-specific configuration parameters.
	 */
	@JsonProperty(PROPERTY_CLEANUP_WORKING_DIR_ON_START)
	public boolean isCleanupWorkingDirOnStart() {
		return cleanupWorkingDirOnStart;
	}

	/**
	 * Sets the cleanup-working-dir-on-start-specific section of the configuration file.
	 */
	@JsonProperty(PROPERTY_CLEANUP_WORKING_DIR_ON_START)
	public void setCleanupWorkingDirOnStart(boolean cleanupWorkingDirOnStart) {
		this.cleanupWorkingDirOnStart = cleanupWorkingDirOnStart;
	}

	/**
	 * Returns the task-execution-threads-specific section of the configuration file.
	 *
	 * @return task-execution-threads-specific configuration parameters.
	 */
	@JsonProperty(PROPERTY_EXEC_THREADS)
	public int getExecutionThreads() {
		return executionThreads;
	}

	/**
	 * Sets the task-execution-threads-specific section of the configuration file.
	 */
	@JsonProperty(PROPERTY_EXEC_THREADS)
	public void setExecutionThreads(int executionThreads) {
		this.executionThreads = executionThreads;
	}

	/**
	 * Returns the max-task-exec-time-specific section of the configuration file.
	 *
	 * @return max-task-exec-time-specific configuration parameters.
	 */
	@JsonProperty(PROPERTY_MAX_TASK_EXEC_TIME)
	public long getMaxTaskExecTime() {
		return maxTaskExecTime;
	}

	/**
	 * Sets the max-task-exec-time-specific section of the configuration file.
	 */
	@JsonProperty(PROPERTY_MAX_TASK_EXEC_TIME)
	public void setMaxTaskExecTime(long maxTaskExecTime) {
		this.maxTaskExecTime = maxTaskExecTime;
	}

	/**
	 * Returns the max-task-exec-unit-specific section of the configuration file.
	 *
	 * @return max-task-exec-unit-specific configuration parameters.
	 */
	@JsonProperty(PROPERTY_MAX_TASK_EXEC_UNIT)
	public TimeUnit getMaxTaskExecUnit() {
		return maxTaskExecUnit;
	}

	/**
	 * Sets the max-task-exec-unit-specific section of the configuration file.
	 */
	@JsonProperty(PROPERTY_MAX_TASK_EXEC_UNIT)
	public void setMaxTaskExecUnit(TimeUnit maxTaskExecUnit) {
		this.maxTaskExecUnit = maxTaskExecUnit;
	}

	/**
	 * Returns the library-endpoint-specific section of the configuration file.
	 *
	 * @return library-endpoint-specific configuration parameters.
	 */
	@JsonProperty(PROPERTY_LIBRARY_ENDPOINT)
	public ServiceEndpointDTO getLibraryEndpoint() {
		return libraryEndpoint;
	}

	/**
	 * Sets the library-endpoint-specific section of the configuration file.
	 */
	@JsonProperty(PROPERTY_LIBRARY_ENDPOINT)
	public void setLibraryEndpoint(ServiceEndpointDTO libraryEndpoint) {
		this.libraryEndpoint = libraryEndpoint;
	}


	@JsonProperty(PROPERTY_CLUSTER_REGISTRATION)
	public boolean isClusterRegistration() {
		return clusterRegistration;
	}

	@JsonProperty(PROPERTY_CLUSTER_REGISTRATION)
	public void setClusterRegistration(boolean clusterRegistration) {
		this.clusterRegistration = clusterRegistration;
	}

	@JsonProperty(PROPERTY_CLUSTER_ENDPOINT)
	public ServiceEndpointDTO getClusterEndpoint() {
		return clusterEndpoint;
	}

	@JsonProperty(PROPERTY_CLUSTER_ENDPOINT)
	public void setClusterEndpoint(ServiceEndpointDTO clusterEndpoint) {
		this.clusterEndpoint = clusterEndpoint;
	}

	@JsonProperty(PROPERTY_MATLAB_RUNTIME)
	public String getMatlabRuntime() {
		return matlabRuntime;
	}

	@JsonProperty(PROPERTY_MATLAB_RUNTIME)
	public void setMatlabRuntime(String matlabRuntime) {
		this.matlabRuntime = matlabRuntime;
	}

	@JsonProperty(PROPERTY_ENVIRONMENT)
	public List<NodeFeatureConfiguration> getEnvironment() {
		return environment;
	}

	@JsonProperty(PROPERTY_ENVIRONMENT)
	public void setEnvironment(List<NodeFeatureConfiguration> environment) {
		this.environment = environment;
	}

	@JsonIgnore
	public static NodeConfiguration getDefault() {
		return new NodeConfiguration() {
			@Override
			public Environment getFeatureEnvironment() {
				Environment environment = new Environment();
				Feature feature = new Feature("java", "1.8", "language", "java ({rbs}:-cp {}) {arg0} {args} {pipes}");
				environment.addFeature(feature);
				return environment;
			}
		};
	}

	@JsonIgnore
	public String getPrimaryFeatureGroup() {
		return PRIMARY_FEATURE_GROUP;
	}

	@JsonIgnore
	public Environment getFeatureEnvironment() {
		Environment environment = new Environment();
		for (NodeFeatureConfiguration nfc : this.environment) {
			environment.addFeature(nfc.getAsFeature());
		}
		return environment;
	}
}
