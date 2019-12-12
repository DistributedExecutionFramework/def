package at.enfilo.def.parameterserver.api.client;

import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;
import at.enfilo.def.parameterserver.api.IParameterServerServiceClient;
import at.enfilo.def.parameterserver.api.rest.IParameterServerResponseService;
import at.enfilo.def.parameterserver.api.rest.IParameterServerService;
import at.enfilo.def.parameterserver.api.thrift.ParameterServerResponseService;
import at.enfilo.def.parameterserver.api.thrift.ParameterServerService;

public class ParameterServerServiceClientFactory extends UnifiedClientFactory<IParameterServerServiceClient> {
    static {
        // Registering unified (I)LibraryService interface.
        register(
                IParameterServerServiceClient.class,
                ParameterServerServiceClient::new,
                IParameterServerService.class,
                IParameterServerResponseService.class,
                IParameterServerService.class,
                ParameterServerService.Client::new,
                IParameterServerResponseService.class,
                ParameterServerResponseService.Client::new
        );
    }

    public ParameterServerServiceClientFactory() {
        super(IParameterServerServiceClient.class);
    }
}