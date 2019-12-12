package at.enfilo.def.parameterserver;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.misc.ServerStartup;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.parameterserver.api.rest.IParameterServerResponseService;
import at.enfilo.def.parameterserver.api.rest.IParameterServerService;
import at.enfilo.def.parameterserver.api.thrift.ParameterServerResponseService;
import at.enfilo.def.parameterserver.api.thrift.ParameterServerService;
import at.enfilo.def.parameterserver.impl.ParameterServerResponseServiceImpl;
import at.enfilo.def.parameterserver.impl.ParameterServerServiceImpl;

import java.util.LinkedList;
import java.util.List;

/**
 * Startup class for Parameter Server.
 */
public class ParameterServer extends ServerStartup<ParameterServerConfiguration> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ParameterServer.class);
    private static final String CONFIG_FILE = "parameter-server.yml";

    private static ParameterServer instance;

    public ParameterServer() {
        super(ParameterServer.class, ParameterServerConfiguration.class, CONFIG_FILE, LOGGER);
    }

    /**
     * Main entry point for Parameter Server services.
     *
     * @param args - arguments
     */
    public static void main(String[] args) {
        LOGGER.info("Startup Parameter Server...");

        try {
            // Start services
            getInstance().startServices();

        } catch (Exception e) {
            LOGGER.error("Parameter Server failed to start.", e);
        }
    }

    @Override
    protected List<ThriftProcessor> getThriftProcessors() {

        List<ThriftProcessor> thriftProcessorList = new LinkedList<>();
        thriftProcessorList.add(
                new ThriftProcessor<>(
                        IParameterServerService.class.getName(),
                        new ParameterServerServiceImpl(),
                        ParameterServerService.Processor<ParameterServerService.Iface>::new)
        );
        thriftProcessorList.add(
                new ThriftProcessor<>(
                        IParameterServerResponseService.class.getName(),
                        new ParameterServerResponseServiceImpl(),
                        ParameterServerResponseService.Processor<ParameterServerResponseService.Iface>::new)
        );
        return thriftProcessorList;
    }

    @Override
    protected List<IResource> getWebResources() {
        List<IResource> resourceList = new LinkedList<>();
        resourceList.add(new ParameterServerServiceImpl());
        resourceList.add(new ParameterServerResponseServiceImpl());
        return resourceList;
    }

    public static ParameterServer getInstance() {
        if (instance == null) {
            instance = new ParameterServer();
        }
        return instance;
    }
}
