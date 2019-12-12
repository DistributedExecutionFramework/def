package at.enfilo.def.config.server.core;

import at.enfilo.def.config.server.api.IConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class DEFServerEndpointConfiguration implements IConfiguration {
    private static final String DEFAULT_ADDRESS = "0.0.0.0";
    private static final int DEFAULT_PORT = 9999;
    private static final String DEFAULT_URL = "/*";
    private static final boolean DEFAULT_ENABLED = false;

    public static final String PROPERTY_ENABLED = "enabled";
    public static final String PROPERTY_BIND_ADDRESS = "bind-address";
    public static final String PROPERTY_PORT = "port";
    public static final String PROPERTY_URL_PATTERN = "url-pattern";

    private boolean enabled;
    @NotNull
    private String bindAddress;
    //@Min(1) @Max(65535)
    private int port;
    @NotNull @Pattern(regexp="[\\w\\d+&@#/%?=~_|!:,.;]*\\*$")
    private String urlPattern;

    public DEFServerEndpointConfiguration() {
        this.enabled = DEFAULT_ENABLED;
        this.bindAddress = DEFAULT_ADDRESS;
        this.port = DEFAULT_PORT;
        this.urlPattern = DEFAULT_URL;
    }

    @JsonProperty(PROPERTY_ENABLED)
    public boolean isEnabled() {
        return enabled;
    }

    @JsonProperty(PROPERTY_ENABLED)
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    @JsonProperty(PROPERTY_PORT)
    public int getPort() {
        return port;
    }

    @JsonProperty(PROPERTY_PORT)
    @Min(1) @Max(65535)
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty(PROPERTY_BIND_ADDRESS)
    public String getBindAddress() {
        return bindAddress;
    }

    @JsonProperty(PROPERTY_BIND_ADDRESS)
    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    @JsonProperty(PROPERTY_URL_PATTERN)
    public String getUrlPattern() {
        return urlPattern;
    }

    @JsonProperty(PROPERTY_URL_PATTERN)
    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    @JsonIgnore
    public static DEFServerEndpointConfiguration getDefault() {
        return new DEFServerEndpointConfiguration();
    }

}
