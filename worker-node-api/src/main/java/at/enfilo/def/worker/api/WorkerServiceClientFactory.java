package at.enfilo.def.worker.api;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.worker.api.rest.IWorkerResponseService;
import at.enfilo.def.worker.api.rest.IWorkerService;
import at.enfilo.def.worker.api.thrift.WorkerResponseService;
import at.enfilo.def.worker.api.thrift.WorkerService;

public class WorkerServiceClientFactory extends UnifiedClientFactory<IWorkerServiceClient> {
	static {
		// Registering unified IManagementResource.
		register(
				IWorkerServiceClient.class,
				WorkerServiceClient::new,
				IWorkerService.class,
				IWorkerResponseService.class,
				WorkerService.class,
				WorkerService.Client::new,
				WorkerResponseService.class,
				WorkerResponseService.Client::new
		);
	}

	public WorkerServiceClientFactory() {
		super(IWorkerServiceClient.class);
	}
}
