package at.enfilo.def.execlogic.api;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.execlogic.api.rest.IExecLogicResponseService;
import at.enfilo.def.execlogic.api.rest.IExecLogicService;
import at.enfilo.def.execlogic.api.thrift.ExecLogicResponseService;
import at.enfilo.def.execlogic.api.thrift.ExecLogicService;

public class ExecLogicServiceClientFactory extends UnifiedClientFactory<IExecLogicServiceClient> {
	static {
		// Registering unified (I)ExecLogicService interface.
		register(
				IExecLogicServiceClient.class,
				ExecLogicServiceClient::new,
				IExecLogicService.class,
				IExecLogicResponseService.class,
				ExecLogicService.class,
				ExecLogicService.Client::new,
				ExecLogicResponseService.class,
				ExecLogicResponseService.Client::new
		);
	}

	public ExecLogicServiceClientFactory() {
		super(IExecLogicServiceClient.class);
	}
}
