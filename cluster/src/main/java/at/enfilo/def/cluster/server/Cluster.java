package at.enfilo.def.cluster.server;

import at.enfilo.def.cluster.api.thrift.ClusterResponseService;
import at.enfilo.def.cluster.api.thrift.ClusterService;
import at.enfilo.def.cluster.impl.*;
import at.enfilo.def.cluster.util.ClusterConfiguration;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.misc.ServerStartup;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.execlogic.api.thrift.ExecLogicResponseService;
import at.enfilo.def.execlogic.api.thrift.ExecLogicService;
import at.enfilo.def.execlogic.impl.ExecLogicResponseServiceImpl;
import at.enfilo.def.execlogic.impl.ExecLogicServiceImpl;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.observer.api.thrift.NodeObserverService;

import java.util.LinkedList;
import java.util.List;

/**
 * Cluster startup class.
 */
public class Cluster extends ServerStartup<ClusterConfiguration> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(Cluster.class);
    private static final String CONFIG_FILE = "cluster.yml";

    private static Cluster instance;

	// Hiding public constructor
    private Cluster() {
        super(Cluster.class, ClusterConfiguration.class, CONFIG_FILE, LOGGER);
    }

	@Override
	protected List<ThriftProcessor> getThriftProcessors() {
		ThriftProcessor<ClusterServiceImpl> clusterServiceProcessor = new ThriftProcessor<>(
			ClusterService.class.getName(),
			new ClusterServiceImpl(),
			ClusterService.Processor<ClusterService.Iface>::new
		);

		ThriftProcessor<ClusterResponseServiceImpl> clusterResponseServiceProcessor = new ThriftProcessor<>(
			ClusterResponseService.class.getName(),
			new ClusterResponseServiceImpl(),
			ClusterResponseService.Processor<ClusterResponseService.Iface>::new
		);

		ThriftProcessor<ExecLogicServiceImpl> execLogicServiceProcessor = new ThriftProcessor<>(
			ExecLogicService.class.getName(),
			new ExecLogicServiceImpl(ClusterExecLogicController.getInstance()),
			ExecLogicService.Processor<ExecLogicService.Iface>::new
		);

		ThriftProcessor<ExecLogicResponseServiceImpl> execLogicResponseServiceProcessor = new ThriftProcessor<>(
			ExecLogicResponseService.class.getName(),
			new ExecLogicResponseServiceImpl(),
			ExecLogicResponseService.Processor<ExecLogicResponseService.Iface>::new
		);

		ThriftProcessor<NodeObserverServiceImpl> workerObserverServiceProcessor = new ThriftProcessor<>(
			NodeObserverService.class.getName(),
			new NodeObserverServiceImpl(),
			NodeObserverService.Processor<NodeObserverService.Iface>::new
		);

		List<ThriftProcessor> thriftProcessors = new LinkedList<>();
		thriftProcessors.add(clusterServiceProcessor);
		thriftProcessors.add(clusterResponseServiceProcessor);
		thriftProcessors.add(execLogicServiceProcessor);
		thriftProcessors.add(execLogicResponseServiceProcessor);
		thriftProcessors.add(workerObserverServiceProcessor);
		return thriftProcessors;
	}

	@Override
	protected List<IResource> getWebResources() {
		List<IResource> webResources = new LinkedList<>();
		webResources.add(new ClusterServiceImpl());
		webResources.add(new ClusterResponseServiceImpl());
		webResources.add(new ExecLogicServiceImpl(ClusterExecLogicController.getInstance()));
		webResources.add(new ExecLogicResponseServiceImpl());
		webResources.add(new NodeObserverServiceImpl());
		return webResources;
	}

    /**
     * Main entry point for ClusterServices.
     * @param args
     */
    public static void main(String[] args) {
        LOGGER.info("Startup Cluster");

        // Create new ClusterResource
        ClusterResource.getInstance();

        // Start services
		getInstance().startServices();
    }

    public static Cluster getInstance() {
		if (instance == null) {
			instance = new Cluster();
		}
		return instance;
	}
}