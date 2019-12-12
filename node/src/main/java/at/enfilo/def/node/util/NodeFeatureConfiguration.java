package at.enfilo.def.node.util;

import at.enfilo.def.common.util.environment.domain.Feature;
import at.enfilo.def.config.server.api.IConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeFeatureConfiguration implements IConfiguration {

    private static final String PROPERTY_FEATURE_NAME = "name";
    private static final String PROPERTY_FEATURE_VERSION = "version";
    private static final String PROPERTY_FEATURE_EXTENSIONS = "ext";
    private static final String PROPERTY_FEATURE_VARIABLES = "var";
    private static final String PROPERTY_FEATURE_CMD = "cmd";
    private static final String PROPERTY_FEATURE_ENVIRONMENT = "env";
    private static final String PROPERTY_FEATURE_GROUP = "group";

    private String name = "";
    private String version = "";
    private String group = "";
    private List<NodeFeatureExtensionConfiguration> extensions = new ArrayList<>();
    private String cmd = "";
    private Map<String, String> environment = new HashMap<>();
    private Map<String, String> variables = new HashMap<>();

    public NodeFeatureConfiguration() {}

    public NodeFeatureConfiguration(NodeFeatureConfiguration cfg, List<NodeFeatureExtensionConfiguration> extensions) {
        this.name = cfg.name;
        this.version = cfg.version;
        this.group = cfg.group;
        this.cmd = cfg.cmd;
        this.environment.putAll(cfg.environment);
        this.variables.putAll(cfg.variables);
        for (NodeFeatureExtensionConfiguration extension : extensions) {
            this.extensions.add(new NodeFeatureExtensionConfiguration(extension));
        }
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

    @JsonProperty(PROPERTY_FEATURE_GROUP)
    public String getGroup() {
        return group;
    }

    @JsonProperty(PROPERTY_FEATURE_GROUP)
    public void setGroup(String group) {
        this.group = group;
    }

    @JsonProperty(PROPERTY_FEATURE_EXTENSIONS)
    public List<NodeFeatureExtensionConfiguration> getExtensions() {
        return extensions;
    }

    @JsonProperty(PROPERTY_FEATURE_EXTENSIONS)
    public void setExtensions(List<NodeFeatureExtensionConfiguration> extensions) {
        this.extensions = extensions;
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
        StringBuilder str = new StringBuilder(name);
        str.append("(").append(version).append(")");
        if (extensions != null && !extensions.isEmpty()) {
            str.append(":");
            for (NodeFeatureExtensionConfiguration extension : extensions) {
                str.append(extension.toString()).append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    public Feature getAsFeature() {
        Feature feature = new Feature(name, version, group, cmd, new HashMap<>(environment), new HashMap<>(variables));
        for (NodeFeatureExtensionConfiguration nfc : extensions) {
            feature.addExtension(nfc.getAsExtension());
        }
        return feature;
    }
}
