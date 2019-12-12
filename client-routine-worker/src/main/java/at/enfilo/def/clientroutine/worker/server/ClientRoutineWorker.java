package at.enfilo.def.clientroutine.worker.server;

import at.enfilo.def.clientroutine.worker.api.thrift.ClientRoutineWorkerResponseService;
import at.enfilo.def.clientroutine.worker.api.thrift.ClientRoutineWorkerService;
import at.enfilo.def.clientroutine.worker.impl.ClientRoutineWorkerResponseServiceImpl;
import at.enfilo.def.clientroutine.worker.impl.ClientRoutineWorkerServiceImpl;
import at.enfilo.def.clientroutine.worker.util.ClientRoutineWorkerConfiguration;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.misc.ServerStartup;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.util.DirectoryCleaner;

import java.util.LinkedList;
import java.util.List;

public class ClientRoutineWorker extends ServerStartup<ClientRoutineWorkerConfiguration> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ClientRoutineWorker.class);
    private static final String CONFIG_FILE = "client-routine-worker.yml";

    private static ClientRoutineWorker instance;

    private ClientRoutineWorker() { super(ClientRoutineWorker.class, ClientRoutineWorkerConfiguration.class, CONFIG_FILE, LOGGER); }

    @Override
    protected List<ThriftProcessor> getThriftProcessors() {
        ThriftProcessor<ClientRoutineWorkerServiceImpl> clientRoutineWorkerServiceProcessor = new ThriftProcessor<ClientRoutineWorkerServiceImpl>(
                ClientRoutineWorkerService.class.getName(),
                new ClientRoutineWorkerServiceImpl(),
                ClientRoutineWorkerService.Processor<ClientRoutineWorkerService.Iface>::new
        );
        ThriftProcessor<ClientRoutineWorkerResponseServiceImpl> clientRoutineWorkerServiceResponseProcessor = new ThriftProcessor<>(
                ClientRoutineWorkerResponseService.class.getName(),
                new ClientRoutineWorkerResponseServiceImpl(),
                ClientRoutineWorkerResponseService.Processor<ClientRoutineWorkerResponseService.Iface>::new
        );

        List<ThriftProcessor> thriftProcessors = new LinkedList<>();
        thriftProcessors.add(clientRoutineWorkerServiceProcessor);
        thriftProcessors.add(clientRoutineWorkerServiceResponseProcessor);
        return thriftProcessors;
    }

    @Override
    protected List<IResource> getWebResources() {
        List<IResource> resourceList = new LinkedList<>();
        resourceList.add(new ClientRoutineWorkerServiceImpl());
        resourceList.add(new ClientRoutineWorkerResponseServiceImpl());
        return resourceList;
    }


    /**
     * ClientRoutine Worker entry point for ClientRoutineWorkerService.
     * @param args
     */
    public static void main(String[] args) {

        LOGGER.info("Startup clientRoutineWorker");

        // Cleanup working dir
        new DirectoryCleaner(LOGGER).cleanWorkingDirectory(getInstance().getConfiguration());

        try {
            // Start services
            getInstance().startServices();
        } catch (Exception e) {
            LOGGER.error("ClientRoutineWorker failed to start.", e);
        }

    }

    public static ClientRoutineWorker getInstance() {
        if (instance == null) {
            instance = new ClientRoutineWorker();
        }
        return instance;
    }

}
