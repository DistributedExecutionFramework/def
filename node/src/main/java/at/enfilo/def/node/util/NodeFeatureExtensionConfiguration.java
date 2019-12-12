package at.enfilo.def.node.util;

import at.enfilo.def.common.util.environment.domain.Extension;
import at.enfilo.def.config.server.api.IConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class NodeFeatureExtensionConfiguration implements IConfiguration {

    private static final String PROPERTY_FEATURE_NAME = "name";
    private static final String PROPERTY_FEATURE_VERSION = "version";
    private static final String PROPERTY_FEATURE_VARIABLES = "var";
    private static final String PROPERTY_FEATURE_CMD = "cmd";
    private static final String PROPERTY_FEATURE_ENVIRONMENT = "env";

    private String name = "";
    private String version = "";
    private String cmd = "";
    private Map<String, String> environment = new HashMap<>();
    private Map<String, String> variables = new HashMap<>();

    public NodeFeatureExtensionConfiguration() {}

    public NodeFeatureExtensionConfiguration(NodeFeatureExtensionConfiguration extension) {
        this.name = extension.name;
        this.version = extension.version;
        this.cmd = extension.cmd;
        this.environment.putAll(extension.environment);
        this.variables.putAll(extension.variables);
    }

    @JsonProperty(PROPERTY_FEATURE_NAME)
    public String getName() {
        return name;
    }

    @JsonProperty(PROPERTY_FEATURE_NAME)
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty(PROPERTY_FEATURE_VERSION)
    public String getVersion() {
        return version;
    }

    @JsonProperty(PROPERTY_FEATURE_VERSION)
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty(PROPERTY_FEATURE_CMD)
    public String getCmd() {
        return cmd;
    }

    @JsonProperty(PROPERTY_FEATURE_CMD)
    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    @JsonProperty(PROPERTY_FEATURE_ENVIRONMENT)
    public Map<String, String> getEnvironment() {
        return environment;
    }

    @JsonProperty(PROPERTY_FEATURE_ENVIRONMENT)
    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    @JsonProperty(PROPERTY_FEATURE_VARIABLES)
    public Map<String, String> getVariables() {
        return variables;
    }

    @JsonProperty(PROPERTY_FEATURE_VARIABLES)
    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return name + "(" + version + ")";
    }

    @JsonIgnore
    public Extension getAsExtension() {
        return new Extension(name, version, cmd, new HashMap<>(environment), new HashMap<>(variables));
    }

}
