package at.enfilo.def.reducer.util;

import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.common.util.environment.domain.Feature;
import at.enfilo.def.node.util.NodeConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReducerConfiguration extends NodeConfiguration {
	private static final String PROPERTY_STORE_ROUTINE = "store-routine";
	private static final String DEFAULT_STORE_ROUTINE = "92979065-f681-32bf-be17-94271ec3900a";

	private String storeRoutineId = DEFAULT_STORE_ROUTINE;

	@JsonProperty(PROPERTY_STORE_ROUTINE)
	public String getStoreRoutineId() {
		return storeRoutineId;
	}

	@JsonProperty(PROPERTY_STORE_ROUTINE)
	public void setStoreRoutineId(String storeRoutineId) {
		this.storeRoutineId = storeRoutineId;
	}

	@JsonIgnore
	public static ReducerConfiguration getDefault() {
        Environment environment = new Environment();
        Feature feature = new Feature("java", "1.8", "language", "java ({rbs}:-cp {}) {arg0} {args} {pipes}");
        environment.addFeature(feature);
        return new ReducerConfiguration() {
            @Override
            public Environment getFeatureEnvironment() {
                return environment;
            }
        };
	}
}
