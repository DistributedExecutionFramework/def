package at.enfilo.def.node.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;
import at.enfilo.def.node.api.thrift.NodeResponseService;
import at.enfilo.def.node.api.thrift.NodeService;

import java.util.LinkedList;
import java.util.List;

public class NodeServiceThriftTest extends NodeServiceTest {
	@Override
	protected IServer getServer(NodeServiceController controller) throws Exception {

		ThriftProcessor nodeServiceProcessor = new ThriftProcessor<>(
				NodeService.class.getName(),
				new NodeServiceImpl(controller, TicketRegistry.getInstance()),
				NodeService.Processor<NodeService.Iface>::new
		);
		ThriftProcessor nodeServiceResponseProcessor = new ThriftProcessor<>(
				NodeResponseService.class.getName(),
				new NodeResponseServiceImpl(),
				NodeResponseService.Processor<NodeResponseService.Iface>::new
		);
		List<ThriftProcessor> processors = new LinkedList<>();
		processors.add(nodeServiceProcessor);
		processors.add(nodeServiceResponseProcessor);

		DEFServerEndpointConfiguration configuration = new DEFServerEndpointConfiguration();
		configuration.setEnabled(true);
		configuration.setPort(40032);
		configuration.setBindAddress("127.0.0.1");
		configuration.setUrlPattern("/*");

		return ThriftTCPServer.getInstance(
				configuration,
				processors
		);
	}
}
