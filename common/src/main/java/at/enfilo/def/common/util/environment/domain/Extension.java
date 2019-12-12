package at.enfilo.def.common.util.environment.domain;

import at.enfilo.def.common.util.environment.VersionMatcher;

import java.util.HashMap;
import java.util.Map;

public class Extension implements IFeature {

    private String name;
    private String version;
    private String cmd;
    private Map<String, String> environment = new HashMap<>();
    private Map<String, String> variables = new HashMap<>();

    public Extension() {
    }

    public Extension(String name) {
        this.name = name;
    }

    public Extension(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public Extension(String name, String version, String cmd) {
        this.name = name;
        this.version = version;
        this.cmd = cmd;
    }

    public Extension(String name, String version, String cmd, Map<String, String> environment, Map<String, String> variables) {
        this.name = name;
        this.version = version;
        this.cmd = cmd;
        this.environment = environment;
        this.variables = variables;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        if (version == null || version.isEmpty()) {
            return name;
        }
        return name + "(" + version + ")";
    }

    public static Extension buildFromString(String extension) {
        if (extension == null || extension.isEmpty()) {
            return null;
        }
        if (extension.contains("(") && extension.contains(")")) {
            if (extension.indexOf(")") - extension.indexOf("(") <= 1) {
                return new Extension(extension.substring(0, extension.indexOf("(")));
            }
            return new Extension(extension.substring(0, extension.indexOf("(")),
                    extension.substring(extension.indexOf("(") + 1, extension.length() - 1));
        } else {
            return new Extension(extension);
        }
    }

    public boolean matches(Extension extension) {
        return extension == null || extension.name == null || extension.name.isEmpty()
                || name.equals(extension.name) && VersionMatcher.matchVersion(version, extension.version);
    }

    public static String getName(String extension) {
        if (extension == null || extension.isEmpty()) {
            return null;
        }

        int idx = extension.length();
        if (extension.contains("(")) {
            idx = extension.indexOf("(");
        }

        return extension.substring(0, idx);
    }
}
