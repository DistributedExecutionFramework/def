package at.enfilo.def.node.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.rest.RESTServer;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;

import java.util.LinkedList;
import java.util.List;

public class NodeServiceRESTTest extends NodeServiceTest {
	@Override
	protected IServer getServer(NodeServiceController controller) throws Exception {
		List<IResource> resources = new LinkedList<>();
		resources.add(new NodeServiceImpl(controller, TicketRegistry.getInstance()));
		resources.add(new NodeResponseServiceImpl());

		DEFServerEndpointConfiguration configuration = new DEFServerEndpointConfiguration();
		configuration.setEnabled(true);
		configuration.setPort(40030);
		configuration.setBindAddress("127.0.0.1");
		configuration.setUrlPattern("/*");

		return RESTServer.getInstance(
				configuration,
				resources
		);
	}
}
