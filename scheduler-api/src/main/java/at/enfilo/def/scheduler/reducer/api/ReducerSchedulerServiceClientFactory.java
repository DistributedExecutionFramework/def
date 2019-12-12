package at.enfilo.def.scheduler.reducer.api;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.scheduler.reducer.api.rest.IReducerSchedulerResponseService;
import at.enfilo.def.scheduler.reducer.api.rest.IReducerSchedulerService;
import at.enfilo.def.scheduler.reducer.api.thrift.ReducerSchedulerResponseService;
import at.enfilo.def.scheduler.reducer.api.thrift.ReducerSchedulerService;

public class ReducerSchedulerServiceClientFactory extends UnifiedClientFactory<IReducerSchedulerServiceClient> {
    static {
        // Registering unified (I)ReducerSchedulerService interface.
        register(
                IReducerSchedulerServiceClient.class,
                ReducerSchedulerServiceClient::new,
                IReducerSchedulerService.class,
                IReducerSchedulerResponseService.class,
                ReducerSchedulerService.class,
                ReducerSchedulerService.Client::new,
                ReducerSchedulerResponseService.class,
                ReducerSchedulerResponseService.Client::new
        );
    }

    public ReducerSchedulerServiceClientFactory() { super(IReducerSchedulerServiceClient.class);}
}
