package at.enfilo.def.client.util;

/**
 * Created by mase on 24.10.2016.
 */
public enum ContextParam {

    HOME_HOST("homeHost"),
    HOME_PORT("homePort"),
    HOME_ADDRESS_BASE("homeAddressBase");

    private final String contextParamKey;

    ContextParam(String contextParamKey) {
        this.contextParamKey = contextParamKey;
    }

    public String getContextParamKey() {
        return contextParamKey;
    }

    @Override
    public String toString() {
        return contextParamKey;
    }
}
