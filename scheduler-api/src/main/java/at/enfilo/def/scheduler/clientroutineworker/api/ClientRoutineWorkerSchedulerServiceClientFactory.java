package at.enfilo.def.scheduler.clientroutineworker.api;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.scheduler.clientroutineworker.api.rest.IClientRoutineWorkerSchedulerResponseService;
import at.enfilo.def.scheduler.clientroutineworker.api.rest.IClientRoutineWorkerSchedulerService;
import at.enfilo.def.scheduler.clientroutineworker.api.thrift.ClientRoutineWorkerSchedulerResponseService;
import at.enfilo.def.scheduler.clientroutineworker.api.thrift.ClientRoutineWorkerSchedulerService;

public class ClientRoutineWorkerSchedulerServiceClientFactory extends UnifiedClientFactory<IClientRoutineWorkerSchedulerServiceClient> {

    static {
        // Registering unified (I)ClientRoutineWorkerSchedulerService interface.
        register(
                IClientRoutineWorkerSchedulerServiceClient.class,
                ClientRoutineWorkerSchedulerServiceClient::new,
                IClientRoutineWorkerSchedulerService.class,
                IClientRoutineWorkerSchedulerResponseService.class,
                ClientRoutineWorkerSchedulerService.class,
                ClientRoutineWorkerSchedulerService.Client::new,
                ClientRoutineWorkerSchedulerResponseService.class,
                ClientRoutineWorkerSchedulerResponseService.Client::new
        );
    }

    public ClientRoutineWorkerSchedulerServiceClientFactory() { super(IClientRoutineWorkerSchedulerServiceClient.class); }
}
