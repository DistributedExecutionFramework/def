package at.enfilo.def.node.observer.api.client.factory;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.rest.INodeObserverService;
import at.enfilo.def.node.observer.api.thrift.NodeObserverService;

/**
 * Client Factory for NodeObserver Service (IBaseNodeObserverServiceClient)
 */
public class NodeObserverServiceClientFactory extends UnifiedClientFactory<INodeObserverServiceClient> {
	static {
		// Registering unified (I)NodeObserverService interface.
		register(
			INodeObserverServiceClient.class,
			NodeObserverServiceClient::new,
			INodeObserverService.class,
			NodeObserverService.class,
			NodeObserverService.Client::new
		);
	}

	public NodeObserverServiceClientFactory() {
		super(INodeObserverServiceClient.class);
	}
}
