package at.enfilo.def.reducer.api;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.reducer.api.rest.IReducerResponseService;
import at.enfilo.def.reducer.api.rest.IReducerService;
import at.enfilo.def.reducer.api.thrift.ReducerResponseService;
import at.enfilo.def.reducer.api.thrift.ReducerService;

/**
 * Created by mase on 20.08.2017.
 */
public class ReducerServiceClientFactory extends UnifiedClientFactory<IReducerServiceClient> {
	static {
		// Registering unified IManagementResource.
		register(
			IReducerServiceClient.class,
			ReducerServiceClient::new,
			IReducerService.class,
			IReducerResponseService.class,
			ReducerService.class,
			ReducerService.Client::new,
			ReducerResponseService.class,
			ReducerResponseService.Client::new
		);
	}

	public ReducerServiceClientFactory() {
		super(IReducerServiceClient.class);
	}
}
