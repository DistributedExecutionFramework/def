package at.enfilo.def.node.api;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.node.api.rest.INodeResponseService;
import at.enfilo.def.node.api.rest.INodeService;
import at.enfilo.def.node.api.thrift.NodeResponseService;
import at.enfilo.def.node.api.thrift.NodeService;

/**
 * Client Factory for Basic "Node" Service
 */
public class NodeServiceClientFactory extends UnifiedClientFactory<INodeServiceClient> {

	static {
		// Registering unified (I)BaseNodeServiceClient interface.
		register(
			INodeServiceClient.class,
			NodeServiceClient::new,
			INodeService.class,
			INodeResponseService.class,
			NodeService.class,
			NodeService.Client::new,
			NodeResponseService.class,
			NodeResponseService.Client::new
		);
	}

	public NodeServiceClientFactory() {
		super(INodeServiceClient.class);
	}
}
