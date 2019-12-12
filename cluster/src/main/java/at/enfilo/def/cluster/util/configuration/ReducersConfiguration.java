package at.enfilo.def.cluster.util.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ReducersConfiguration extends NodesConfiguration {

	@JsonIgnore
	public static ReducersConfiguration getDefault() {
		return new ReducersConfiguration();
	}
}
