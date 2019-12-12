package at.enfilo.def.communication.api.common.client;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.common.api.IThrowingFunction;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

/**
 * Created by mase on 03.10.2016.
 */
public abstract class ServiceClient<T> implements IClient<T> {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ServiceClient.class);
	private static final int RETRIES = 3;
	private static final int WAIT_BETWEEN_RETRIES = 3 * 1000; // in ms
    private final ServiceEndpointDTO serviceEndpoint;

    protected ServiceClient(ServiceEndpointDTO serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    @Override
    public ServiceEndpointDTO getServiceEndpoint() {
        return serviceEndpoint;
    }

	@Override
	public void executeVoid(IThrowingConsumer<T> proxy) throws ClientCommunicationException {
    	int i = 0;
    	while (true) {
    		try {
    			exec(proxy);
    			break;
			} catch (ClientCommunicationException e) {
    			i++;
				LOGGER.error("Communication error occurred. Tries: {}/{}.", i, RETRIES);
    			close();
    			if (i >= RETRIES) {
    				throw e;
				} else {
					try {
						Thread.sleep(WAIT_BETWEEN_RETRIES);
					} catch (InterruptedException ex) {
						LOGGER.error("Interrupted.", e);
						Thread.currentThread().interrupt();
						throw new ClientCommunicationException(ex);
					}
				}
			}
		}
	}

	@Override
	public <R> R execute(IThrowingFunction<T, R> proxy) throws ClientCommunicationException {
		int i = 0;
		while (true) {
			try {
				return exec(proxy);
			} catch (ClientCommunicationException e) {
				i++;
				LOGGER.error("Communication error occurred. Tries: {}/{}.", i, RETRIES);
				close();
				if (i >= RETRIES) {
					throw e;
				} else {
					try {
						Thread.sleep(WAIT_BETWEEN_RETRIES);
					} catch (InterruptedException ex) {
						LOGGER.error("Interrupted.", ex);
						Thread.currentThread().interrupt();
						throw new ClientCommunicationException(ex);
					}
				}
			}
		}
	}

	protected abstract void exec(IThrowingConsumer<T> proxy) throws ClientCommunicationException;
	protected abstract <R> R exec(IThrowingFunction<T, R> proxy) throws ClientCommunicationException;
}
