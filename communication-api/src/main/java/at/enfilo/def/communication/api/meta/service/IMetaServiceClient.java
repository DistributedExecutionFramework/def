package at.enfilo.def.communication.api.meta.service;

import at.enfilo.def.communication.exception.ClientCommunicationException;

public interface IMetaServiceClient {
	/**
	 * Returns version of service
	 */
	String getVersion() throws ClientCommunicationException;

	/**
	 * Returns current timestamp (in millis) of service host.
	 */
	long getTime() throws ClientCommunicationException;

	/**
	 * Close client.
	 */
	void close();
}
