package at.enfilo.def.local.simulator;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.misc.ServerStartup;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.execlogic.api.thrift.ExecLogicResponseService;
import at.enfilo.def.execlogic.api.thrift.ExecLogicService;
import at.enfilo.def.execlogic.impl.ExecLogicResponseServiceImpl;
import at.enfilo.def.execlogic.impl.ExecLogicServiceImpl;
import at.enfilo.def.execlogic.impl.IExecLogicController;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class ExecLogicServiceStarter extends ServerStartup<SimulatorConfiguration> {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(Simulator.class);
	private static final String CONFIG_FILE = "simulator.yml";

	private final ExecLogicServiceImpl execLogicService;
	private final ExecLogicResponseServiceImpl execLogicResponseService;

	public ExecLogicServiceStarter(IExecLogicController execLogicController) {
		super(Simulator.class, SimulatorConfiguration.class, CONFIG_FILE, LOGGER);
		this.execLogicService = new ExecLogicServiceImpl(execLogicController);
		this.execLogicResponseService = new ExecLogicResponseServiceImpl();
	}

	@Override
	protected List<ThriftProcessor> getThriftProcessors() {
		List<ThriftProcessor> resources = new LinkedList<>();
		ThriftProcessor<ExecLogicServiceImpl> execLogicServiceProcessor = new ThriftProcessor<>(
				ExecLogicService.class.getName(),
				execLogicService,
				ExecLogicService.Processor<ExecLogicService.Iface>::new
		);
		resources.add(execLogicServiceProcessor);

		ThriftProcessor<ExecLogicResponseServiceImpl> execLogicResponseServiceProcessor = new ThriftProcessor<>(
				ExecLogicResponseService.class.getName(),
				execLogicResponseService,
				ExecLogicResponseService.Processor<ExecLogicResponseService.Iface>::new
		);
		resources.add(execLogicResponseServiceProcessor);

		return resources;
	}

	@Override
	protected List<IResource> getWebResources() {
		List<IResource> resources = new LinkedList<>();
		resources.add(execLogicService);
		resources.add(execLogicResponseService);
		return resources;
	}
}
