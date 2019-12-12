package at.enfilo.def.manager.api;

import at.enfilo.def.communication.api.common.client.IServiceClient;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.transfer.dto.AuthDTO;

import java.util.concurrent.Future;

public interface IAuthServiceClient extends IServiceClient {
	/**
	 * Authentication - returns a token if login was successful.
	 *
	 * @param name - user name
	 * @param password - user password
	 * @return - token object
	 * @throws ClientCommunicationException
	 */
	Future<AuthDTO> getToken(String name, String password) throws ClientCommunicationException;
}
