package at.enfilo.def.cluster.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkersConfiguration extends NodesConfiguration {

	private static final String PROPERTY_STORE_ROUTINE = "store-routine";

	private String storeRoutineId = "bae3723e-51b2-391f-9abd-b2ac750c1932";

	@JsonProperty(PROPERTY_STORE_ROUTINE)
	public String getStoreRoutineId() {
		return storeRoutineId;
	}

	@JsonProperty(PROPERTY_STORE_ROUTINE)
	public void setStoreRoutineId(String storeRoutineId) {
		this.storeRoutineId = storeRoutineId;
	}

	@JsonIgnore
	public static WorkersConfiguration getDefault() {
		return new WorkersConfiguration();
	}
}
