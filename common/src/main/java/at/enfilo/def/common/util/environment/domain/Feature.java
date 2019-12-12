package at.enfilo.def.common.util.environment.domain;

import at.enfilo.def.common.util.environment.VersionMatcher;

import java.util.*;
import java.util.stream.Collectors;

public class Feature implements IFeature {

    private String name;
    private String version;
    private String group;
    private List<Extension> extensions = new ArrayList<>();
    private String cmd;
    private Map<String, String> environment = new HashMap<>();
    private Map<String, String> variables = new HashMap<>();

    public Feature() {
    }

    public Feature(String name) {
        this.name = name;
    }

    public Feature(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public Feature(String name, String version, String group, String cmd) {
        this.name = name;
        this.version = version;
        this.group = group;
        this.cmd = cmd;
    }

    public Feature(String name, String version, String group, String cmd, Map<String, String> environment, Map<String, String> variables) {
        this.name = name;
        this.version = version;
        this.group = group;
        this.cmd = cmd;
        this.environment = environment;
        this.variables = variables;
    }

    public Feature(String name, String version, String group, List<Extension> extensions, String cmd, Map<String, String> environment, Map<String, String> variables) {
        this.name = name;
        this.version = version;
        this.group = group;
        this.extensions = extensions;
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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<Extension> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<Extension> extensions) {
        this.extensions = extensions;
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

    public void addExtension(Extension extension) {
        if (extension == null) {
            return;
        }
        if (extensions == null) {
            extensions = new ArrayList<>();
        }
        extensions.add(extension);
    }

    @Override
    public String toString() {
        if(name == null || name.isEmpty()) {
            return null;
        }
        StringBuilder str = new StringBuilder(name);
        if(version != null && !version.isEmpty()) {
            str.append("(").append(version).append(")");
        }
        if (extensions != null && !extensions.isEmpty()) {
            str.append(":");
            for (Extension extension : extensions) {
                str.append(extension.toString()).append(",");
            }
            str.deleteCharAt(str.length() - 1);
        }
        return str.toString();
    }

    public static Feature buildFromString(String feature) {
        if (feature == null || feature.isEmpty()) {
            return null;
        }

        if (feature.contains(":")) {
            String[] split = feature.split(":");
            Feature f = parseFeature(split[0]);
            if (split.length == 1) {
                return f;
            }

            String ext = split[1];
            while (ext != null && !ext.isEmpty()) {
                for (int i = 0; i < ext.length(); i++) {
                    if (i == ext.length() - 1) {
                        f.addExtension(Extension.buildFromString(ext));
                        return f;
                    }
                    if (ext.charAt(i) == ',' && !(ext.indexOf("(") < i && ext.indexOf(")") > i)) {
                        f.addExtension(Extension.buildFromString(ext.substring(0, i)));
                        ext = ext.substring(i + 1);
                        break;
                    }
                }
            }
            return f;
        } else {
            return parseFeature(feature);
        }
    }

    public static String getName(String feature) {
        if (feature == null || feature.isEmpty()) {
            return null;
        }

        int idx = feature.length();
        if (feature.contains("(")) {
            idx = feature.indexOf("(");
        }
        if (feature.contains(":") && feature.indexOf(":") < idx) {
            idx = feature.indexOf(":");
        }

        return feature.substring(0, idx);
    }

    private static Feature parseFeature(String feature) {
        if (feature.contains("(") && feature.contains(")")) {
            if (feature.indexOf(")") - feature.indexOf("(") <= 1) {
                return new Feature(feature.substring(0, feature.indexOf("(")));
            }
            return new Feature(feature.substring(0, feature.indexOf("(")),
                    feature.substring(feature.indexOf("(") + 1, feature.length() - 1));
        } else {
            return new Feature(feature);
        }
    }

    public boolean matches(Feature feature) {
        if (feature == null || feature.getName() == null || feature.getName().isEmpty()) {
            return true;
        }
        if (this.name == null || this.name.isEmpty()) {
            return false;
        }
        if (!this.name.equals(feature.getName()) || !VersionMatcher.matchVersion(this.version, feature.getVersion())) {
            return false;
        }
        for (Extension extension : feature.getExtensions()) {
            if (!matches(extension)) {
                return false;
            }
        }
        return true;
    }

    private boolean matches(Extension extension) {
        for (Extension eExtension : extensions) {
            if (eExtension.matches(extension)) {
                return true;
            }
        }
        return false;
    }

    public List<Extension> getMatching(Extension extension) {
        if (extensions == null || extensions.isEmpty()) {
            return new ArrayList<>();
        }
        if(extension == null || extension.getName() == null || extension.getName().isEmpty()) {
            return extensions;
        }
        List<Extension> matching = new ArrayList<>();
        for (Extension ex : extensions) {
            if (ex.matches(extension)) {
                matching.add(ex);
            }
        }
        return matching;
    }

    public static List<String> getExtensionNames(String feature) {
        if (feature == null || feature.isEmpty() || !feature.contains(":") || feature.indexOf(":") >= feature.length() - 1) {
            return null;
        }

        String[] extensions = feature.substring(feature.indexOf(":") + 1, feature.length()).split(",");
        return Arrays.stream(extensions).map(Extension::getName).collect(Collectors.toList());
    }
}

