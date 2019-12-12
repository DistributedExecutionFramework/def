package at.enfilo.def.communication.api.common.client;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.common.api.IThrowingFunction;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;

public class DirectJavaClient<T> extends ServiceClient<T> {

	private T service;

	public DirectJavaClient(T service) {
		super(new ServiceEndpointDTO("localhost", -1, Protocol.DIRECT));
		this.service = service;
	}

	@Override
	protected void exec(IThrowingConsumer<T> proxy) throws ClientCommunicationException {
		proxy.accept(service);
	}

	@Override
	protected <R> R exec(IThrowingFunction<T, R> proxy) throws ClientCommunicationException {
		try {
			return proxy.apply(service);
		} catch (Exception e) {
			throw new ClientCommunicationException(e);
		}
	}

	public void setService(T service) {
		this.service = service;
	}

	@Override
	public void close() {

	}
}
