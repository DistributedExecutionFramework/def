package at.enfilo.def.manager.server;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.misc.ServerStartup;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.execlogic.api.thrift.ExecLogicResponseService;
import at.enfilo.def.execlogic.api.thrift.ExecLogicService;
import at.enfilo.def.execlogic.impl.ExecLogicResponseServiceImpl;
import at.enfilo.def.execlogic.impl.ExecLogicServiceImpl;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.manager.api.thrift.AuthResponseService;
import at.enfilo.def.manager.api.thrift.AuthService;
import at.enfilo.def.manager.api.thrift.ManagerResponseService;
import at.enfilo.def.manager.api.thrift.ManagerService;
import at.enfilo.def.manager.impl.*;
import at.enfilo.def.manager.util.ManagerConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mase on 17.08.2016.
 * <p>
 * Startup class for Manager.
 */
public class Manager extends ServerStartup<ManagerConfiguration> {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(Manager.class);
	private static final String CONFIG_FILE = "manager.yml";

	private static Manager instance;

	/**
	 * Avoid instancing
	 */
	private Manager() {
		super(Manager.class, ManagerConfiguration.class, CONFIG_FILE, LOGGER);
	}

	/**
	 * Main entry point for ManagerServices.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		LOGGER.info("Startup Manager");

		try {
			// Start services
			getInstance().startServices();
		} catch (Exception e) {
			LOGGER.error("Manager failed to start.", e);
		}
	}

	@Override
	protected List<ThriftProcessor> getThriftProcessors() {
		ThriftProcessor authProcessor = new ThriftProcessor<>(
				AuthService.class.getName(),
				new AuthServiceImpl(),
				AuthService.Processor<AuthService.Iface>::new
		);
		ThriftProcessor authResponseProcessor = new ThriftProcessor<>(
				AuthResponseService.class.getName(),
				new AuthResponseServiceImpl(),
				AuthResponseService.Processor<AuthResponseService.Iface>::new
		);
		ThriftProcessor managerProcessor = new ThriftProcessor<>(
				ManagerService.class.getName(),
				new ManagerServiceImpl(),
				ManagerService.Processor<ManagerService.Iface>::new
		);
		ThriftProcessor managerResponseProcessor = new ThriftProcessor<>(
				ManagerResponseService.class.getName(),
				new ManagerResponseServiceImpl(),
				ManagerResponseService.Processor<ManagerResponseService.Iface>::new
		);
		ThriftProcessor execLogicProcessor = new ThriftProcessor<>(
				ExecLogicService.class.getName(),
				new ExecLogicServiceImpl(ManagerExecLogicController.getInstance()),
				ExecLogicService.Processor<ExecLogicService.Iface>::new
		);
		ThriftProcessor execLogicResponseProcessor = new ThriftProcessor<>(
				ExecLogicResponseService.class.getName(),
				new ExecLogicResponseServiceImpl(),
				ExecLogicResponseService.Processor<ExecLogicResponseService.Iface>::new
		);

		List<ThriftProcessor> thriftProcessorList = new LinkedList<>();
		thriftProcessorList.add(authProcessor);
		thriftProcessorList.add(authResponseProcessor);
		thriftProcessorList.add(managerProcessor);
		thriftProcessorList.add(managerResponseProcessor);
		thriftProcessorList.add(execLogicProcessor);
		thriftProcessorList.add(execLogicResponseProcessor);
		return thriftProcessorList;
	}

	@Override
	protected List<IResource> getWebResources() {
		List<IResource> resourceList = new LinkedList<>();
		resourceList.add(new AuthServiceImpl());
		resourceList.add(new AuthResponseServiceImpl());
		resourceList.add(new ManagerServiceImpl());
		resourceList.add(new ManagerResponseServiceImpl());
		resourceList.add(new ExecLogicServiceImpl(ManagerExecLogicController.getInstance()));
		resourceList.add(new ExecLogicResponseServiceImpl());
		return resourceList;
	}

	public static Manager getInstance() {
		if (instance == null) {
			instance = new Manager();
		}
		return instance;
	}
}
