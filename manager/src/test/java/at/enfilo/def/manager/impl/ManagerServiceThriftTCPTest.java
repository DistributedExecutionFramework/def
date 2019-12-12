package at.enfilo.def.manager.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.manager.api.thrift.ManagerResponseService;
import at.enfilo.def.manager.api.thrift.ManagerService;
import at.enfilo.def.manager.server.Manager;

import java.util.LinkedList;
import java.util.List;

public class ManagerServiceThriftTCPTest extends ManagerServiceTest {

	@Override
	protected IServer getServer() throws ServerCreationException {
		ITicketRegistry ticketRegistry = TicketRegistry.getInstance();
		ManagerServiceImpl managerService = new ManagerServiceImpl(ticketRegistry, managerController);
		ManagerResponseServiceImpl managerResponseService = new ManagerResponseServiceImpl(ticketRegistry);

		ThriftProcessor<ManagerServiceImpl> managerServiceProcessor = new ThriftProcessor<>(
				ManagerService.class.getName(),
				managerService,
				ManagerService.Processor<ManagerService.Iface>::new
		);
		ThriftProcessor<ManagerResponseServiceImpl> managerResponseServiceProcessor = new ThriftProcessor<>(
				ManagerResponseService.class.getName(),
				managerResponseService,
				ManagerResponseService.Processor<ManagerResponseService.Iface>::new
		);

		List<ThriftProcessor> thriftProcessors = new LinkedList<>();
		thriftProcessors.add(managerServiceProcessor);
		thriftProcessors.add(managerResponseServiceProcessor);

		return ThriftTCPServer.getInstance(
			Manager.getInstance().getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration(),
			thriftProcessors
		);
	}
}
