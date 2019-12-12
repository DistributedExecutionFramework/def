package at.enfilo.def.manager.util;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.config.server.core.DEFRootConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by mase on 29.08.2016.
 */
public class ManagerConfiguration extends DEFRootConfiguration {

    public static final String PROPERTY_CLOUDCOMMUNICATION_ENDPOINT = "cloud-communication-endpoint";
    public static final String PROPERTY_CLOUD_CLUSTER_ENDPOINT = "cloud-cluster-endpoint";
    public static final String PROPERTY_CLOUD_LIBRARY_ENDPOINT = "cloud-library-endpoint";
    public static final String PROPERTY_LIBRARY_ENDPOINT = "library-endpoint";
    public static final String PROPERTY_VPN_IP = "vpn-ip";

    public static final String CLOUD_COMMUNICATION_DEFAULT_HOST = "127.0.0.1";
    public static final int CLOUD_COMMUNICATION_DEFAULT_PORT = 40052;
    public static final Protocol CLOUD_COMMUNICATION_DEFAULT_PROTOCOL = Protocol.THRIFT_TCP;

    public static final String CLOUD_CLUSTER_DEFAULT_HOST = "127.0.0.1";
    public static final int CLOUD_CLUSTER_DEFAULT_PORT = 40012;
    public static final Protocol CLOUD_CLUSTER_DEFAULT_PROTOCOL = Protocol.THRIFT_TCP;

    public static final String CLOUD_LIBRARY_DEFAULT_HOST = "127.0.0.1";
    public static final int CLOUD_LIBRARY_DEFAULT_PORT = 40042;
    public static final Protocol CLOUD_LIBRARY_DEFAULT_PROTOCOL = Protocol.THRIFT_TCP;

    public static final String LIBRARY_DEFAULT_HOST = "127.0.0.1";
    public static final int LIBRARY_DEFAULT_PORT = 40042;
    public static final Protocol LIBRARY_DEFAULT_PROTOCOL = Protocol.THRIFT_TCP;


    private ServiceEndpointDTO cloudCommunicationEndpoint = new ServiceEndpointDTO(
            CLOUD_COMMUNICATION_DEFAULT_HOST,
            CLOUD_COMMUNICATION_DEFAULT_PORT,
            CLOUD_COMMUNICATION_DEFAULT_PROTOCOL
    );

    private ServiceEndpointDTO cloudClusterEndpoint = new ServiceEndpointDTO(
            CLOUD_CLUSTER_DEFAULT_HOST,
            CLOUD_CLUSTER_DEFAULT_PORT,
            CLOUD_CLUSTER_DEFAULT_PROTOCOL
    );

    private ServiceEndpointDTO cloudLibraryEndpoint = new ServiceEndpointDTO(
            CLOUD_LIBRARY_DEFAULT_HOST,
            CLOUD_LIBRARY_DEFAULT_PORT,
            CLOUD_LIBRARY_DEFAULT_PROTOCOL
    );

    private ServiceEndpointDTO libraryEndpoint = new ServiceEndpointDTO(
            LIBRARY_DEFAULT_HOST,
            LIBRARY_DEFAULT_PORT,
            LIBRARY_DEFAULT_PROTOCOL
    );

    private String vpnIp;

    @JsonProperty(PROPERTY_VPN_IP)
    public String getVpnIp() { return vpnIp; }

    @JsonProperty(PROPERTY_VPN_IP)
    public void setVpnIp(String vpnIp) {
        this.vpnIp = vpnIp;
    }

    @JsonProperty(PROPERTY_CLOUDCOMMUNICATION_ENDPOINT)
    public ServiceEndpointDTO getCloudCommunicationEndpoint() {
        return cloudCommunicationEndpoint;
    }

    @JsonProperty(PROPERTY_CLOUDCOMMUNICATION_ENDPOINT)
    public void setCloudCommunicationEndpoint(ServiceEndpointDTO cloudCommunicationEndpoint) {
        this.cloudCommunicationEndpoint = cloudCommunicationEndpoint;
    }

    @JsonProperty(PROPERTY_CLOUD_CLUSTER_ENDPOINT)
    public ServiceEndpointDTO getCloudClusterEndpoint() {
        return cloudClusterEndpoint;
    }

    @JsonProperty(PROPERTY_CLOUD_CLUSTER_ENDPOINT)
    public void setCloudClusterEndpoint(ServiceEndpointDTO cloudClusterEndpoint) {
        this.cloudClusterEndpoint = cloudClusterEndpoint;
    }

    @JsonProperty(PROPERTY_CLOUD_LIBRARY_ENDPOINT)
    public ServiceEndpointDTO getCloudLibraryEndpoint() {
        return cloudLibraryEndpoint;
    }

    @JsonProperty(PROPERTY_CLOUD_LIBRARY_ENDPOINT)
    public void setCloudLibraryEndpoint(ServiceEndpointDTO cloudLibraryEndpoint) {
        this.cloudLibraryEndpoint = cloudLibraryEndpoint;
    }

    @JsonProperty(PROPERTY_LIBRARY_ENDPOINT)
    public ServiceEndpointDTO getLibraryEndpoint() {
        return libraryEndpoint;
    }

    @JsonProperty(PROPERTY_LIBRARY_ENDPOINT)
    public void setLibraryEndpoint(ServiceEndpointDTO libraryEndpoint) {
        this.libraryEndpoint = libraryEndpoint;
    }
}
