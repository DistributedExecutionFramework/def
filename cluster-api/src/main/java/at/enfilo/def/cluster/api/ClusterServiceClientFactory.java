package at.enfilo.def.cluster.api;

import at.enfilo.def.cluster.api.rest.IClusterResponseService;
import at.enfilo.def.cluster.api.rest.IClusterService;
import at.enfilo.def.cluster.api.thrift.ClusterResponseService;
import at.enfilo.def.cluster.api.thrift.ClusterService;
import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;

public class ClusterServiceClientFactory extends UnifiedClientFactory<IClusterServiceClient> {
	static {
		// Registering unified (I)ClusterService interface.
		register(
				IClusterServiceClient.class,
				ClusterServiceClient::new,
				IClusterService.class,
				IClusterResponseService.class,
				ClusterService.class,
				ClusterService.Client::new,
				ClusterResponseService.class,
				ClusterResponseService.Client::new
		);
	}

	public ClusterServiceClientFactory() {
		super(IClusterServiceClient.class);
	}
}
