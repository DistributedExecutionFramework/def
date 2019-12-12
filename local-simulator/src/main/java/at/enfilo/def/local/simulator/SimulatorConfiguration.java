package at.enfilo.def.local.simulator;

import at.enfilo.def.config.server.api.IConfiguration;
import at.enfilo.def.config.server.core.DEFRootConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SimulatorConfiguration extends DEFRootConfiguration implements IConfiguration {
	private static final String PROPERTY_NAME_DEFAULT_MAP_ROUTINE = "default-map-routine";
	private static final String DEFAULT_VALUE_DEFAULT_MAP_ROUTINE = "4e339e30-cd45-3101-8bb1-39f18895846a";

	private static final String PROPERTY_NAME_PARTITION_ROUTINE = "partition-routine";
	private static final String DEFAULT_VALUE_PARTITION_ROUTINE = "57a85f00-cfbd-36cd-93d0-c06ada13d3d2";

	private static final String PROPERTY_NAME_STORE_ROUTINE = "store-routine";
	private static final String DEFAULT_VALUE_STORE_ROUTINE = "92979065-f681-32bf-be17-94271ec3900a";

	private static final String PROPERTY_NAME_WORKING_DIR = "working-dir";
	private static final String DEFAULT_VALUE_WORKING_DIR = "/tmp/def";

	private static final String PROPERTY_MATLAB_RUNTIME = "matlab-runtime";
	private static final String DEFAULT_VALUE_MATLAB_RUNTIME = "/usr/local/MATLAB/MATLAB_Runtime/v94";

	private String partitionRoutine = DEFAULT_VALUE_PARTITION_ROUTINE;
	private String storeRoutine = DEFAULT_VALUE_STORE_ROUTINE;
	private String defaultMapRoutine =  DEFAULT_VALUE_DEFAULT_MAP_ROUTINE;
	private String workingDir = DEFAULT_VALUE_WORKING_DIR;
	private String matlabRuntime = DEFAULT_VALUE_MATLAB_RUNTIME;

	@JsonProperty(PROPERTY_NAME_PARTITION_ROUTINE)
	public String getPartitionRoutine() {
		return partitionRoutine;
	}

	@JsonProperty(PROPERTY_NAME_PARTITION_ROUTINE)
	public void setPartitionRoutine(String partitionRoutine) {
		this.partitionRoutine = partitionRoutine;
	}

	@JsonProperty(PROPERTY_NAME_STORE_ROUTINE)
	public String getStoreRoutine() {
		return storeRoutine;
	}

	@JsonProperty(PROPERTY_NAME_STORE_ROUTINE)
	public void setStoreRoutine(String storeRoutine) {
		this.storeRoutine = storeRoutine;
	}

	@JsonProperty(PROPERTY_NAME_DEFAULT_MAP_ROUTINE)
	public String getDefaultMapRoutine() {
		return defaultMapRoutine;
	}

	@JsonProperty(PROPERTY_NAME_DEFAULT_MAP_ROUTINE)
	public void setDefaultMapRoutine(String defaultMapRoutine) {
		this.defaultMapRoutine = defaultMapRoutine;
	}

	@JsonProperty(PROPERTY_NAME_WORKING_DIR)
	public String getWorkingDir() {
		return workingDir;
	}

	@JsonProperty(PROPERTY_NAME_WORKING_DIR)
	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	@JsonProperty(PROPERTY_MATLAB_RUNTIME)
	public String getMatlabRuntime() {
		return matlabRuntime;
	}

	@JsonProperty(PROPERTY_MATLAB_RUNTIME)
	public void setMatlabRuntime(String matlabRuntime) {
		this.matlabRuntime = matlabRuntime;
	}
}
