package at.enfilo.def.communication.mock;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.common.api.IThrowingFunction;
import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;

/**
 * Created by mase on 29.09.2016.
 */
public class ExecutorClientMock<T> implements IClient<T> {

	@Override
	public ServiceEndpointDTO getServiceEndpoint() {
		return null;
	}

	@Override
    public void executeVoid(IThrowingConsumer<T> proxy)
    throws ClientCommunicationException {
        try {
            proxy.accept(null);
        } catch (Exception e) {
            throw new ClientCommunicationException(e);
        }
    }

    @Override
    public <R> R execute(IThrowingFunction<T, R> proxy)
    throws ClientCommunicationException {
        try {
            return proxy.apply(null);
        } catch (Exception e) {
            throw new ClientCommunicationException(e);
        }
    }

	@Override
	public void close() {

	}
}
