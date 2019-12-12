package at.enfilo.def.manager.webservice.util;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.config.server.core.DEFRootConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ManagerWebserviceConfiguration extends DEFRootConfiguration {
	private static final String PROPERTY_MANAGER_ENDPOINT = "manager-endpoint";
	private static final ServiceEndpointDTO DEFAULT_MANAGER_ENDPOINT = new ServiceEndpointDTO("localhost", 40002, Protocol.THRIFT_TCP);

	private static final String PROPERTY_EXECLOGIC_ENDPOINT = "execlogic-endpoint";
	private static final ServiceEndpointDTO DEFAULT_EXECLOGIC_ENDPOINT = DEFAULT_MANAGER_ENDPOINT;

	private static final String PROPERTY_LIBRARY_ENDPOINT = "library-endpoint";
	private static final ServiceEndpointDTO DEFAULT_LIBRARY_ENDPOINT = new ServiceEndpointDTO("localhost", 40042, Protocol.THRIFT_TCP);

	private ServiceEndpointDTO managerEndpoint = DEFAULT_MANAGER_ENDPOINT;
	private ServiceEndpointDTO execLogicEndpoint = DEFAULT_EXECLOGIC_ENDPOINT;
	private ServiceEndpointDTO libraryEndpoint = DEFAULT_LIBRARY_ENDPOINT;

	@JsonProperty(PROPERTY_MANAGER_ENDPOINT)
	public ServiceEndpointDTO getManagerEndpoint() {
		return managerEndpoint;
	}

	@JsonProperty(PROPERTY_MANAGER_ENDPOINT)
	public void setManagerEndpoint(ServiceEndpointDTO managerEndpoint) {
		this.managerEndpoint = managerEndpoint;
	}

	@JsonProperty(PROPERTY_EXECLOGIC_ENDPOINT)
	public ServiceEndpointDTO getExecLogicEndpoint() {
		return execLogicEndpoint;
	}

	@JsonProperty(PROPERTY_EXECLOGIC_ENDPOINT)
	public void setExecLogicEndpoint(ServiceEndpointDTO execLogicEndpoint) {
		this.execLogicEndpoint = execLogicEndpoint;
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
