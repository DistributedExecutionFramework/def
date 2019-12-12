package at.enfilo.def.communication.api.meta.service;

import at.enfilo.def.communication.api.common.client.IClient;
import at.enfilo.def.communication.api.meta.thrift.MetaService;
import at.enfilo.def.communication.exception.ClientCommunicationException;

class MetaServiceClient implements IMetaServiceClient {
	private final IClient<? extends MetaService.Iface> client;

	public MetaServiceClient(IClient<? extends MetaService.Iface> client) {
		this.client = client;
	}

	@Override
	public String getVersion() throws ClientCommunicationException {
		return client.execute(client -> client.getVersion());
	}

	@Override
	public long getTime() throws ClientCommunicationException {
		return client.execute(client -> client.getTime());
	}

	@Override
	public void close() {
		client.close();
	}
}
