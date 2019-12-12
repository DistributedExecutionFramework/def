package at.enfilo.def.scheduler.worker.api;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.scheduler.worker.api.rest.IWorkerSchedulerResponseService;
import at.enfilo.def.scheduler.worker.api.rest.IWorkerSchedulerService;
import at.enfilo.def.scheduler.worker.api.thrift.WorkerSchedulerResponseService;
import at.enfilo.def.scheduler.worker.api.thrift.WorkerSchedulerService;

public class WorkerSchedulerServiceClientFactory extends UnifiedClientFactory<IWorkerSchedulerServiceClient> {

    static {
        // Registering unified (I)WorkerSchedulerService interface.
        register(
                IWorkerSchedulerServiceClient.class,
                WorkerSchedulerServiceClient::new,
                IWorkerSchedulerService.class,
                IWorkerSchedulerResponseService.class,
                WorkerSchedulerService.class,
                WorkerSchedulerService.Client::new,
                WorkerSchedulerResponseService.class,
                WorkerSchedulerResponseService.Client::new
        );
    }

    public WorkerSchedulerServiceClientFactory() { super(IWorkerSchedulerServiceClient.class); }
}
