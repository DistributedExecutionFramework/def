package at.enfilo.def.manager.api;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.manager.api.rest.IAuthResponseService;
import at.enfilo.def.manager.api.rest.IAuthService;
import at.enfilo.def.manager.api.thrift.AuthResponseService;
import at.enfilo.def.manager.api.thrift.AuthService;

public class AuthServiceClientFactory extends UnifiedClientFactory<IAuthServiceClient> {
	static {
		// Registering unified (I)ClusterService interface.
		register(
				IAuthServiceClient.class,
				AuthServiceClient::new,
				IAuthService.class,
				IAuthResponseService.class,
				AuthService.class,
				AuthService.Client::new,
				AuthResponseService.class,
				AuthResponseService.Client::new
		);
	}

	public AuthServiceClientFactory() {
		super(IAuthServiceClient.class);
	}
}
