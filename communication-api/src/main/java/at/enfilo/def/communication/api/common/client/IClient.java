package at.enfilo.def.communication.api.common.client;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.common.api.IThrowingFunction;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;

/**
 * Created by mase on 31.08.2016.
 */
public interface IClient<T> {

    String DEFAULT_PATH_PREFIX = "/";
    String DEFAULT_RESOLVED_PREFIX = "";

	/**
	 * Returns information of service endpoint (host, port, protocol, path).
	 *
	 * @return ServiceEndpoint object.
	 */
	ServiceEndpointDTO getServiceEndpoint();

    /**
     * Performs (accepts) given proxy function on instance of client.
     *
     * @param proxy proxy function to be performed.
     * @throws ClientCommunicationException
     */
    void executeVoid(IThrowingConsumer<T> proxy) throws ClientCommunicationException;

    /**
     * Executes (applies) given proxy function on instance of client.
     *
     * @param proxy proxy function to be executed.
     * @param <R> generic type that represents expected type of the result to be returned.
     * @throws ClientCommunicationException
     */
    <R> R execute(IThrowingFunction<T, R> proxy) throws ClientCommunicationException;

	/**
	 * Close client.
	 */
	void close();
}
