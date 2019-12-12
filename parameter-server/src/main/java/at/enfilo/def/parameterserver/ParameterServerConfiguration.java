package at.enfilo.def.parameterserver;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.config.server.core.DEFRootConfiguration;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import at.enfilo.def.parameterserver.impl.store.driver.simple.SimpleStoreDriver;

import java.net.MalformedURLException;
import java.net.URL;

public class ParameterServerConfiguration extends DEFRootConfiguration {

    private static final String PROPERTY_CLUSTER_REGISTRATION = "cluster-registration";
    private static final String PROPERTY_CLUSTER_ENDPOINT = "cluster-endpoint";
    private static final String PROPERTY_STORE_DRIVER = "store-driver";
    private static final String PROPERTY_STORE_ENDPOINT_URL = "store-endpoint-url";

    private String storeDriver = SimpleStoreDriver.class.getName();
    private String storeEndpointUrl = "";

    private boolean clusterRegistration = false;
    private ServiceEndpointDTO clusterEndpoint = null;

    /**
     * Returns the store-driver-specific section of the configuration file.
     *
     * @return store-driver-specific configuration parameters.
     */
    @JsonProperty(PROPERTY_STORE_DRIVER)
    public String getStoreDriver() {
        return storeDriver;
    }

    /**
     * Sets the store-driver-specific section of the configuration file.
     */
    @JsonProperty(PROPERTY_STORE_DRIVER)
    public void setStoreDriver(String storeDriver) {
        this.storeDriver = storeDriver;
    }

    /**
     * Returns the store-endpoint-url-specific section of the configuration file.
     *
     * @return store-endpoint-url-specific configuration parameters.
     */
    @JsonProperty(PROPERTY_STORE_ENDPOINT_URL)
    public URL getStoreEndpointUrl() throws MalformedURLException {
        return new URL(storeEndpointUrl);
    }

    /**
     * Sets the store-endpoint-url-specific section of the configuration file.
     */
    @JsonProperty(PROPERTY_STORE_ENDPOINT_URL)
    public void setStoreEndpointUrl(String storeEndpointUrl) {
        this.storeEndpointUrl = storeEndpointUrl;
    }

    /**
     * Sets the store-endpoint-url-specific section of the configuration file.
     */
    @JsonProperty(PROPERTY_STORE_ENDPOINT_URL)
    public void setStoreEndpointUrl(URL storeEndpointUrl) {
        this.storeEndpointUrl = storeEndpointUrl.toString();
    }

    @JsonProperty(PROPERTY_CLUSTER_REGISTRATION)
    public boolean isClusterRegistration() {
        return clusterRegistration;
    }

    @JsonProperty(PROPERTY_CLUSTER_REGISTRATION)
    public void setClusterRegistration(boolean clusterRegistration) {
        this.clusterRegistration = clusterRegistration;
    }

    @JsonProperty(PROPERTY_CLUSTER_ENDPOINT)
    public ServiceEndpointDTO getClusterEndpoint() {
        return clusterEndpoint;
    }

    @JsonProperty(PROPERTY_CLUSTER_ENDPOINT)
    public void setClusterEndpoint(ServiceEndpointDTO clusterEndpoint) {
        this.clusterEndpoint = clusterEndpoint;
    }

    @JsonIgnore
    public static ParameterServerConfiguration getDefault() {
        return new ParameterServerConfiguration();
    }
}
