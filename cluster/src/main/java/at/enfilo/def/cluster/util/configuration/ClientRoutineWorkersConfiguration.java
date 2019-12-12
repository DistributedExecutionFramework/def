package at.enfilo.def.cluster.util.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ClientRoutineWorkersConfiguration extends NodesConfiguration {

    @JsonIgnore
    public static ClientRoutineWorkersConfiguration getDefault() { return new ClientRoutineWorkersConfiguration(); }
}
