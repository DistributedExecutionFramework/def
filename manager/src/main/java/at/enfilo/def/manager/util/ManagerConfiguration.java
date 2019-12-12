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
    public static final String PROPERTY_CLUSTER_ENDPOINT = "cluster-endpoint";
    public static final String PROPERTY_LIBRARY_ENDPOINT = "library-endpoint";
    public static final String PROPERTY_VPN_IP = "vpn-ip";

    public static final String CLOUDCOMMUNICATION_DEFAULT_HOST = "127.0.0.1";
    public static final int CLOUDCOMMUNICATION_DEFAULT_PORT = 40052;
    public static final Protocol CLOUDCOMMUNICATION_DEFAULT_PROTOCOL = Protocol.THRIFT_TCP;

    public static final String CLUSTER_DEFAULT_HOST = "127.0.0.1";
    public static final int CLUSTER_DEFAULT_PORT = 40012;
    public static final Protocol CLUSTER_DEFAULT_PROTOCOL = Protocol.THRIFT_TCP;

    public static final String LIBRARY_DEFAULT_HOST = "127.0.0.1";
    public static final int LIBRARY_DEFAULT_PORT = 40042;
    public static final Protocol LIBRARY_DEFAULT_PROTOCOL = Protocol.THRIFT_TCP;

    private ServiceEndpointDTO cloudCommunicationEndpoint = new ServiceEndpointDTO(
            CLOUDCOMMUNICATION_DEFAULT_HOST,
            CLOUDCOMMUNICATION_DEFAULT_PORT,
            CLOUDCOMMUNICATION_DEFAULT_PROTOCOL
    );

    private ServiceEndpointDTO clusterEndpoint = new ServiceEndpointDTO(
            CLUSTER_DEFAULT_HOST,
            CLUSTER_DEFAULT_PORT,
            CLUSTER_DEFAULT_PROTOCOL
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

    @JsonProperty(PROPERTY_CLUSTER_ENDPOINT)
    public ServiceEndpointDTO getClusterEndpoint() {
        return clusterEndpoint;
    }

    @JsonProperty(PROPERTY_CLUSTER_ENDPOINT)
    public void setClusterEndpoint(ServiceEndpointDTO clusterEndpoint) {
        this.clusterEndpoint = clusterEndpoint;
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
