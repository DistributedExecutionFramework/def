package at.enfilo.def.clientroutine.worker.api;

import at.enfilo.def.clientroutine.worker.api.rest.IClientRoutineWorkerResponseService;
import at.enfilo.def.clientroutine.worker.api.rest.IClientRoutineWorkerService;
import at.enfilo.def.clientroutine.worker.api.thrift.ClientRoutineWorkerResponseService;
import at.enfilo.def.clientroutine.worker.api.thrift.ClientRoutineWorkerService;
import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;

public class ClientRoutineWorkerServiceClientFactory extends UnifiedClientFactory<IClientRoutineWorkerServiceClient> {
    static {
        // Registering unified IManagementResources
        register(
                IClientRoutineWorkerServiceClient.class,
                ClientRoutineWorkerServiceClient::new,
                IClientRoutineWorkerService.class,
                IClientRoutineWorkerResponseService.class,
                ClientRoutineWorkerService.class,
                ClientRoutineWorkerService.Client::new,
                ClientRoutineWorkerResponseService.class,
                ClientRoutineWorkerResponseService.Client::new
        );
    }

    public ClientRoutineWorkerServiceClientFactory() { super(IClientRoutineWorkerServiceClient.class); }
}
