package at.enfilo.def.manager.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.rest.RESTServer;
import at.enfilo.def.manager.server.Manager;

import java.util.LinkedList;
import java.util.List;

public class AuthServiceRESTTest extends AuthServiceTest {

	@Override
	protected IServer getServer() throws ServerCreationException {
		ITicketRegistry ticketRegistry = TicketRegistry.getInstance();
		AuthServiceImpl authService = new AuthServiceImpl(authController, ticketRegistry);
		AuthResponseServiceImpl authResponseService = new AuthResponseServiceImpl(ticketRegistry);

		List<IResource> resourceList = new LinkedList<>();
		resourceList.add(authService);
		resourceList.add(authResponseService);

		return RESTServer.getInstance(
			Manager.getInstance().getConfiguration().getServerHolderConfiguration().getRESTConfiguration(),
			resourceList
		);
	}
}
