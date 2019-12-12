package at.enfilo.def.client.api;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.execlogic.api.ExecLogicServiceClientFactory;
import at.enfilo.def.execlogic.api.IExecLogicServiceClient;
import at.enfilo.def.manager.api.IManagerServiceClient;
import at.enfilo.def.manager.api.ManagerServiceClientFactory;

public class DEFClientFactory {
	public static final Protocol DEFAULT_PROTOCOL = Protocol.REST;
	public static final String DEFAULT_URL_PATTERN = "/api/*";

	public static IDEFClient createClient(String host, int port) throws ClientCreationException {
		return createClient(host, port, DEFAULT_PROTOCOL, DEFAULT_URL_PATTERN);
	}

	public static IDEFClient createClient(String host, int port, Protocol protocol, String urlPattern)
	throws ClientCreationException {
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(host, port, protocol);
		endpoint.setPathPrefix(urlPattern);
		return createClient(endpoint);
	}

	public static IDEFClient createClient(ServiceEndpointDTO endpoint) throws ClientCreationException {
		IManagerServiceClient managerServiceClient = new ManagerServiceClientFactory().createClient(endpoint);
		IExecLogicServiceClient execLogicServiceClient = new ExecLogicServiceClientFactory().createClient(endpoint);
		return new DEFClient(managerServiceClient, execLogicServiceClient);
	}
}
