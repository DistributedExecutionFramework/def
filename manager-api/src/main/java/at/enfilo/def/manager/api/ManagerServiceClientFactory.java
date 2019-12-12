package at.enfilo.def.manager.api;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.manager.api.rest.IManagerResponseService;
import at.enfilo.def.manager.api.rest.IManagerService;
import at.enfilo.def.manager.api.thrift.ManagerResponseService;
import at.enfilo.def.manager.api.thrift.ManagerService;

public class ManagerServiceClientFactory extends UnifiedClientFactory<IManagerServiceClient> {
	static {
		// Registering unified (I)ClusterService interface.
		register(
				IManagerServiceClient.class,
				ManagerServiceClient::new,
				IManagerService.class,
				IManagerResponseService.class,
				ManagerService.class,
				ManagerService.Client::new,
				ManagerResponseService.class,
				ManagerResponseService.Client::new
		);
	}

	public ManagerServiceClientFactory() {
		super(IManagerServiceClient.class);
	}
}
