package at.enfilo.def.reducer.server;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.misc.ServerStartup;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.util.DirectoryCleaner;
import at.enfilo.def.reducer.api.thrift.ReducerResponseService;
import at.enfilo.def.reducer.api.thrift.ReducerService;
import at.enfilo.def.reducer.impl.ReducerResponseServiceImpl;
import at.enfilo.def.reducer.impl.ReducerServiceImpl;
import at.enfilo.def.reducer.util.ReducerConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mase on 28.08.2017.
 */
public class Reducer extends ServerStartup<ReducerConfiguration> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(Reducer.class);
    private static final String CONFIG_FILE = "reducer.yml";

    private static Reducer instance;

    /**
     * Avoid instancing
     */
    private Reducer() {
        super(Reducer.class, ReducerConfiguration.class, CONFIG_FILE, LOGGER);
    }

    /**
     * Main entry point for ReduceServices.
     *
     * @param args
     */
    public static void main(String[] args) {
        LOGGER.info("Startup Reducer.");

        // Cleanup working dir
        new DirectoryCleaner(LOGGER).cleanWorkingDirectory(getInstance().getConfiguration());

        try {
            // Start services
            getInstance().startServices();
        } catch (Exception e) {
            LOGGER.error("Reducer failed to start.", e);
        }
    }

    @Override
    protected List<ThriftProcessor> getThriftProcessors() {
        ThriftProcessor reduceServiceProcessor = new ThriftProcessor<>(
            ReducerService.class.getName(),
            new ReducerServiceImpl(),
            ReducerService.Processor<ReducerService.Iface>::new
        );

        ThriftProcessor reduceResponseServiceProcessor = new ThriftProcessor<>(
            ReducerResponseService.class.getName(),
            new ReducerResponseServiceImpl(),
            ReducerResponseService.Processor<ReducerResponseService.Iface>::new
        );

        List<ThriftProcessor> thriftProcessorList = new LinkedList<>();
        thriftProcessorList.add(reduceServiceProcessor);
        thriftProcessorList.add(reduceResponseServiceProcessor);
        return thriftProcessorList;
    }

    @Override
    protected List<IResource> getWebResources() {
        List<IResource> resourceList = new LinkedList<>();
        resourceList.add(new ReducerServiceImpl());
        resourceList.add(new ReducerResponseServiceImpl());
        return resourceList;
    }

    public static Reducer getInstance() {
        if (instance == null) {
            instance = new Reducer();
        }
        return instance;
    }
}
