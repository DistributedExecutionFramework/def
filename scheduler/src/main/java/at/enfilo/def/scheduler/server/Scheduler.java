package at.enfilo.def.scheduler.server;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.misc.ServerStartup;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.node.observer.api.thrift.NodeObserverService;
import at.enfilo.def.scheduler.api.thrift.SchedulerResponseService;
import at.enfilo.def.scheduler.api.thrift.SchedulerService;
import at.enfilo.def.scheduler.impl.NodeObserverServiceImpl;
import at.enfilo.def.scheduler.impl.SchedulerResponseServiceImpl;
import at.enfilo.def.scheduler.impl.SchedulerServiceImpl;
import at.enfilo.def.scheduler.util.SchedulerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class Scheduler extends ServerStartup<SchedulerConfiguration> {
	private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
	private static final String CONFIG_FILE = "scheduler.yml";

	private static Scheduler instance;

	public Scheduler() {
		super(Scheduler.class, SchedulerConfiguration.class, CONFIG_FILE, LOGGER);
	}

	public static void main(String[] args) {
		LOGGER.info("Startup Scheduler");

		getInstance().startServices();
	}

	@Override
	protected List<ThriftProcessor> getThriftProcessors() {
		ThriftProcessor<SchedulerServiceImpl> schedulerServiceProcessor = new ThriftProcessor<>(
			SchedulerService.class.getName(),
			new SchedulerServiceImpl(),
			SchedulerService.Processor<SchedulerService.Iface>::new
		);

		ThriftProcessor<SchedulerResponseServiceImpl> schedulerResponseServiceProcessor = new ThriftProcessor<>(
			SchedulerResponseService.class.getName(),
			new SchedulerResponseServiceImpl(),
			SchedulerResponseService.Processor<SchedulerResponseService.Iface>::new
		);

		ThriftProcessor<NodeObserverServiceImpl> workerObserverServiceProcessor = new ThriftProcessor<>(
			NodeObserverService.class.getName(),
			new NodeObserverServiceImpl(),
			NodeObserverService.Processor<NodeObserverService.Iface>::new
		);

		List<ThriftProcessor> processors = new LinkedList<>();
		processors.add(schedulerServiceProcessor);
		processors.add(schedulerResponseServiceProcessor);
		processors.add(workerObserverServiceProcessor);
		return processors;
	}

	@Override
	protected List<IResource> getWebResources() {
		List<IResource> resourceList = new LinkedList<>();
		resourceList.add(new SchedulerServiceImpl());
		resourceList.add(new SchedulerResponseServiceImpl());
		resourceList.add(new NodeObserverServiceImpl());
		return resourceList;
	}

	public static Scheduler getInstance() {
		if (instance == null) {
			instance = new Scheduler();
		}
		return instance;
	}
}
