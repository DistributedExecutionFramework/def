package at.enfilo.def.config.server.core;

import at.enfilo.def.config.server.api.IConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by mase on 09.09.2016.
 */
public class DEFServerHolderConfiguration implements IConfiguration {

    public static final String PROPERTY_REST = "rest";
    public static final String PROPERTY_THRIFT_TCP = "thrift-tcp";
    public static final String PROPERTY_THRIFT_HTTP = "thrift-http";

    private DEFServerEndpointConfiguration restConfiguration;
    private DEFServerEndpointConfiguration thriftTCPConfiguration;
    private DEFServerEndpointConfiguration thriftHTTPConfiguration;

    /**
     * Returns the rest-specific section of the configuration file.
     *
     * @return rest-specific configuration parameters.
     */
    @JsonProperty(PROPERTY_REST)
    public DEFServerEndpointConfiguration getRESTConfiguration() {
        return restConfiguration != null ? restConfiguration : DEFServerEndpointConfiguration.getDefault();
    }

    /**
     * Sets the rest-specific section of the configuration file.
     */
    @JsonProperty(PROPERTY_REST)
    public void setRESTConfiguration(DEFServerEndpointConfiguration restConfiguration) {
        this.restConfiguration = restConfiguration;
    }

    /**
     * Returns the thrift-tcp-specific section of the configuration file.
     *
     * @return thrift-tcp-specific configuration parameters.
     */
    @JsonProperty(PROPERTY_THRIFT_TCP)
    public DEFServerEndpointConfiguration getThriftTCPConfiguration() {
        return thriftTCPConfiguration != null ? thriftTCPConfiguration : DEFServerEndpointConfiguration.getDefault();
    }

    /**
     * Sets the thrift-tcp-specific section of the configuration file.
     */
    @JsonProperty(PROPERTY_THRIFT_TCP)
    public void setThriftTCPConfiguration(DEFServerEndpointConfiguration thriftTCPConfiguration) {
        this.thriftTCPConfiguration = thriftTCPConfiguration;
    }

    /**
     * Returns the thrift-http-specific section of the configuration file.
     *
     * @return thrift-http-specific configuration parameters.
     */
    @JsonProperty(PROPERTY_THRIFT_HTTP)
    public DEFServerEndpointConfiguration getThriftHTTPConfiguration() {
        return thriftHTTPConfiguration != null ? thriftHTTPConfiguration : DEFServerEndpointConfiguration.getDefault();
    }

    /**
     * Sets the thrift-http-specific section of the configuration file.
     */
    @JsonProperty(PROPERTY_THRIFT_HTTP)
    public void setThriftHTTPConfiguration(DEFServerEndpointConfiguration thriftHTTPConfiguration) {
        this.thriftHTTPConfiguration = thriftHTTPConfiguration;
    }

    @JsonIgnore
    public static DEFServerHolderConfiguration getDefault() {
        return new DEFServerHolderConfiguration();
    }
}
