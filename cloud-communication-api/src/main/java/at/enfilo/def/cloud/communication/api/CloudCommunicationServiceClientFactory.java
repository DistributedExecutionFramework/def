package at.enfilo.def.cloud.communication.api;

import at.enfilo.def.cloud.communication.api.rest.ICloudCommunicationResponseService;
import at.enfilo.def.cloud.communication.api.rest.ICloudCommunicationService;
import at.enfilo.def.cloud.communication.api.thrift.CloudCommunicationResponseService;
import at.enfilo.def.cloud.communication.api.thrift.CloudCommunicationService;
import at.enfilo.def.communication.api.common.factory.UnifiedClientFactory;

public class CloudCommunicationServiceClientFactory extends UnifiedClientFactory<ICloudCommunicationServiceClient> {
    static {
        register(
                ICloudCommunicationServiceClient.class,
                CloudCommunicationServiceClient::new,
                ICloudCommunicationService.class,
                ICloudCommunicationResponseService.class,
                CloudCommunicationService.class,
                CloudCommunicationService.Client::new,
                CloudCommunicationResponseService.class,
                CloudCommunicationResponseService.Client::new
        );
    }

    public CloudCommunicationServiceClientFactory() { super(ICloudCommunicationServiceClient.class); }
}