package at.enfilo.def.cloud.communication.impl;

import at.enfilo.def.cloud.communication.api.rest.ICloudCommunicationResponseService;
import at.enfilo.def.cloud.communication.api.thrift.CloudCommunicationResponseService;
import at.enfilo.def.communication.impl.ResponseService;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.util.List;

public class CloudCommunicationResponseServiceImpl extends ResponseService
implements ICloudCommunicationResponseService, CloudCommunicationResponseService.Iface{

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(CloudCommunicationResponseServiceImpl.class);

    public CloudCommunicationResponseServiceImpl() { super(LOGGER); }

    @Override
    public String createAWSCluster(String ticketId) {
        return getResult(ticketId, String.class);
    }

    @Override
    public String bootClusterInstance(String ticketId) {
        return getResult(ticketId, String.class);
    }

    @Override
    public List<String> bootNodes(String ticketId) {
        return getResult(ticketId, List.class);
    }

    @Override
    public String getPublicIPAddressOfCloudInstance(String ticketId) {
        return getResult(ticketId, String.class);
    }

    @Override
    public String getPrivateIPAddressOfCloudInstance(String ticketId) {
        return getResult(ticketId, String.class);
    }
}
