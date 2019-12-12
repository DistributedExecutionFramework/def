package at.enfilo.def.reducer.util;

import at.enfilo.def.common.util.environment.domain.Environment;
import at.enfilo.def.common.util.environment.domain.Feature;
import at.enfilo.def.node.util.NodeConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ReducerConfiguration extends NodeConfiguration {

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
