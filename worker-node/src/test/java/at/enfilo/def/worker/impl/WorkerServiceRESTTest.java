package at.enfilo.def.worker.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.rest.RESTServer;
import at.enfilo.def.worker.server.Worker;

import java.util.LinkedList;
import java.util.List;

public class WorkerServiceRESTTest extends WorkerServiceTest {

	@Override
	protected IServer getServer(WorkerServiceController controller) throws Exception {
		List<IResource> resources = new LinkedList<>();
		resources.add(new WorkerServiceImpl(controller, TicketRegistry.getInstance()));
		resources.add(new WorkerResponseServiceImpl());

		return RESTServer.getInstance(
				Worker.getInstance().getConfiguration().getServerHolderConfiguration().getRESTConfiguration(),
				resources
		);
	}

}
