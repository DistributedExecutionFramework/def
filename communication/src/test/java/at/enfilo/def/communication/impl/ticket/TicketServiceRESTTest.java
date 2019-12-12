package at.enfilo.def.communication.impl.ticket;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.rest.RESTServer;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;

import java.util.LinkedList;
import java.util.List;

public class TicketServiceRESTTest extends TicketServiceTest {
	@Override
	protected IServer getServer(ITicketRegistry ticketRegistry) throws Exception {
		TicketServiceImpl impl = new TicketServiceImpl(ticketRegistry);
		List<IResource> resourceList = new LinkedList<>();
		resourceList.add(impl);

		DEFServerEndpointConfiguration conf = new DEFServerEndpointConfiguration();
		conf.setPort(9996);
		conf.setBindAddress("127.0.0.1");
		conf.setUrlPattern("/*");

		return RESTServer.getInstance(conf, resourceList, false);
	}
}
