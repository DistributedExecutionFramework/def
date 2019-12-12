package at.enfilo.def.cloud.communication.server;

import at.enfilo.def.cloud.communication.api.rest.ICloudCommunicationResponseService;
import at.enfilo.def.cloud.communication.api.rest.ICloudCommunicationService;
import at.enfilo.def.cloud.communication.api.thrift.CloudCommunicationResponseService;
import at.enfilo.def.cloud.communication.api.thrift.CloudCommunicationService;
import at.enfilo.def.cloud.communication.impl.CloudCommunicationResponseServiceImpl;
import at.enfilo.def.cloud.communication.impl.CloudCommunicationServiceImpl;
import at.enfilo.def.cloud.communication.util.CloudCommunicationConfiguration;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.misc.ServerStartup;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class CloudCommunication extends ServerStartup<CloudCommunicationConfiguration> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(CloudCommunication.class);
    private static final String CONFIG_FILE = "cloud-communication.yml";

    private static CloudCommunication instance;

    /**
     * Avoid instancing
     */
    private CloudCommunication() { super(CloudCommunication.class, CloudCommunicationConfiguration.class, CONFIG_FILE, LOGGER); }

    /**
     * Main entry point for CloudCommunicationServices
     *
     * @param args
     */
    public static void main(String[] args) {
       LOGGER.info("Startup CloudCommunication");

       try {
           getInstance().startServices();
       } catch (Exception e) {
           LOGGER.error("CloudCommunication failed to start.", e);
       }
    }

    @Override
    protected List<ThriftProcessor> getThriftProcessors() {
        ThriftProcessor<CloudCommunicationServiceImpl> cloudCommunicationServiceProcessor = new ThriftProcessor<>(
                CloudCommunicationService.class.getCanonicalName(),
                new CloudCommunicationServiceImpl(),
                CloudCommunicationService.Processor<CloudCommunicationService.Iface>::new
        );

        ThriftProcessor<CloudCommunicationResponseServiceImpl> cloudCommunicationResponseServiceProcessor = new ThriftProcessor<>(
                CloudCommunicationResponseService.class.getCanonicalName(),
                new CloudCommunicationResponseServiceImpl(),
                CloudCommunicationResponseService.Processor<CloudCommunicationResponseService.Iface>::new
        );

        List<ThriftProcessor> thriftProcessorList = new LinkedList<>();
        thriftProcessorList.add(cloudCommunicationServiceProcessor);
        thriftProcessorList.add(cloudCommunicationResponseServiceProcessor);
        return thriftProcessorList;
    }

    @Override
    protected List<IResource> getWebResources() {
        List<IResource> resourceList = new LinkedList<>();
        resourceList.add(new CloudCommunicationServiceImpl());
        resourceList.add(new CloudCommunicationResponseServiceImpl());
        return resourceList;
    }

    public static CloudCommunication getInstance() {
        if (instance == null) {
            instance = new CloudCommunication();
        }
        return instance;
    }
}
