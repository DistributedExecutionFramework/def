package at.enfilo.def.manager.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.manager.api.thrift.AuthResponseService;
import at.enfilo.def.manager.api.thrift.AuthService;
import at.enfilo.def.manager.server.Manager;

import java.util.LinkedList;
import java.util.List;

public class AuthServiceThriftTCPTest extends AuthServiceTest {

	@Override
	protected IServer getServer() throws ServerCreationException {
		ITicketRegistry ticketRegistry = TicketRegistry.getInstance();
		AuthServiceImpl authService = new AuthServiceImpl(authController, ticketRegistry);
		AuthResponseServiceImpl authResponseService = new AuthResponseServiceImpl(ticketRegistry);

		ThriftProcessor<AuthServiceImpl> authServiceProcessor = new ThriftProcessor<>(
				AuthService.class.getName(),
				authService,
				AuthService.Processor<AuthService.Iface>::new
		);
		ThriftProcessor<AuthResponseServiceImpl> authResponseServiceProcessor = new ThriftProcessor<>(
				AuthResponseService.class.getName(),
				authResponseService,
				AuthResponseService.Processor<AuthResponseService.Iface>::new
		);

		List<ThriftProcessor> thriftProcessors = new LinkedList<>();
		thriftProcessors.add(authServiceProcessor);
		thriftProcessors.add(authResponseServiceProcessor);

		return ThriftTCPServer.getInstance(
			Manager.getInstance().getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration(),
			thriftProcessors
		);
	}
}
