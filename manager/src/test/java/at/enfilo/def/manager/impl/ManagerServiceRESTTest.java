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

public class ManagerServiceRESTTest extends ManagerServiceTest {

	@Override
	protected IServer getServer() throws ServerCreationException {
		ITicketRegistry ticketRegistry = TicketRegistry.getInstance();
		ManagerServiceImpl managerService = new ManagerServiceImpl(ticketRegistry, managerController);
		ManagerResponseServiceImpl managerResponseService = new ManagerResponseServiceImpl(ticketRegistry);

		List<IResource> resourceList = new LinkedList<>();
		resourceList.add(managerService);
		resourceList.add(managerResponseService);

		return RESTServer.getInstance(
			Manager.getInstance().getConfiguration().getServerHolderConfiguration().getRESTConfiguration(),
			resourceList
		);
	}
}
