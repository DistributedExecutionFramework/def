package at.enfilo.def.communication.api.common.client;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;

/**
 * Base interface for every service client
 */
public interface IServiceClient {

	/**
	 * Returns service endpoint of this client.
	 *
	 * @return ServiceEndpoint instance
	 */
	ServiceEndpointDTO getServiceEndpoint();

	/**
	 * Close client connection.
	 */
	void close();
}
