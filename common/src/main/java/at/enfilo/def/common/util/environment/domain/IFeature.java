package at.enfilo.def.common.util.environment.domain;

import java.util.Map;

public interface IFeature {

    public String getName();
    public String getVersion();
    public String getCmd();
    public Map<String, String> getEnvironment();
    public Map<String, String> getVariables();
}
