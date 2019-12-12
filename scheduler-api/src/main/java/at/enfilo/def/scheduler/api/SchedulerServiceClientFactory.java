package at.enfilo.def.scheduler.api;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.scheduler.api.rest.ISchedulerResponseService;
import at.enfilo.def.scheduler.api.rest.ISchedulerService;
import at.enfilo.def.scheduler.api.thrift.SchedulerResponseService;
import at.enfilo.def.scheduler.api.thrift.SchedulerService;

/**
 * Factory class for creating ISchedulerServiceClient.
 */
public class SchedulerServiceClientFactory extends UnifiedClientFactory<ISchedulerServiceClient> {
	static {
		// Registering unified (I)SchedulerService interface.
		register(
			ISchedulerServiceClient.class,
			SchedulerServiceClient::new,
			ISchedulerService.class,
			ISchedulerResponseService.class,
			SchedulerService.class,
			SchedulerService.Client::new,
			SchedulerResponseService.class,
			SchedulerResponseService.Client::new
		);
	}

	public SchedulerServiceClientFactory() {
		super(ISchedulerServiceClient.class);
	}
}
