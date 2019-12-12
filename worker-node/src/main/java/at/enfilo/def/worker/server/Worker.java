package at.enfilo.def.worker.server;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.misc.ServerStartup;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.thrift.NodeService;
import at.enfilo.def.node.impl.NodeServiceImpl;
import at.enfilo.def.node.util.DirectoryCleaner;
import at.enfilo.def.worker.api.thrift.WorkerResponseService;
import at.enfilo.def.worker.api.thrift.WorkerService;
import at.enfilo.def.worker.impl.WorkerServiceImpl;
import at.enfilo.def.worker.impl.WorkerResponseServiceImpl;
import at.enfilo.def.worker.util.WorkerConfiguration;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

/**
 * Startup class for WorkerService(s).
 */
public class Worker extends ServerStartup<WorkerConfiguration> {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(Worker.class);
	private static final String CONFIG_FILE = "worker.yml";

	private static Worker instance;

	/**
	 * Avoid instancing
	 */
	private Worker() {
		super(Worker.class, WorkerConfiguration.class, CONFIG_FILE, LOGGER);
	}


	@Override
	protected List<ThriftProcessor> getThriftProcessors() {
		ThriftProcessor<WorkerServiceImpl> workerServiceProcessor = new ThriftProcessor<>(
				WorkerService.class.getName(),
				new WorkerServiceImpl(),
				WorkerService.Processor<WorkerService.Iface>::new
		);
		ThriftProcessor<WorkerResponseServiceImpl> workerServiceResponseProcessor = new ThriftProcessor<>(
				WorkerResponseService.class.getName(),
				new WorkerResponseServiceImpl(),
				WorkerResponseService.Processor<WorkerResponseService.Iface>::new
		);

		List<ThriftProcessor> thriftProcessors = new LinkedList<>();
		thriftProcessors.add(workerServiceProcessor);
		thriftProcessors.add(workerServiceResponseProcessor);
		return thriftProcessors;
	}

	@Override
	protected List<IResource> getWebResources() {
		List<IResource> resourceList = new LinkedList<>();
		resourceList.add(new WorkerServiceImpl());
		resourceList.add(new WorkerResponseServiceImpl());
		return resourceList;
	}

	/**
	 * Worker entry point for WorkerService.
	 * @param args
	 */
	public static void main(String[] args) {

		LOGGER.info("Startup worker");

		// Cleanup working dir
		new DirectoryCleaner(LOGGER).cleanWorkingDirectory(getInstance().getConfiguration());

		// Start services
		try {
			getInstance().startServices();
		} catch (Exception e) {
			LOGGER.error("Worker failed to start.", e);
		}

	}

	public static Worker getInstance() {
		if (instance == null) {
			instance = new Worker();
		}
		return instance;
	}
}
