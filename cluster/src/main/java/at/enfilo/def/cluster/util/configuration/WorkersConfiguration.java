package at.enfilo.def.cluster.util.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WorkersConfiguration extends NodesConfiguration {

	@JsonIgnore
	public static WorkersConfiguration getDefault() {
		return new WorkersConfiguration();
	}
}
