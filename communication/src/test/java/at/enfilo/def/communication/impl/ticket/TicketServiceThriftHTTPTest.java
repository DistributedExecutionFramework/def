package at.enfilo.def.communication.impl.ticket;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.ticket.ITicketRegistry;
import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.http.ThriftHTTPServer;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;

import java.util.Collections;
import java.util.List;

public class TicketServiceThriftHTTPTest extends TicketServiceTest {
	@Override
	protected IServer getServer(ITicketRegistry ticketRegistry) throws Exception {
		TicketServiceImpl impl = new TicketServiceImpl(ticketRegistry);
		ThriftProcessor processor = new ThriftProcessor<>(
											TicketService.class.getName(),
											impl,
											TicketService.Processor<TicketService.Iface>::new
		);
		List<ThriftProcessor> processors = Collections.singletonList(processor);


		DEFServerEndpointConfiguration conf = new DEFServerEndpointConfiguration();
		conf.setPort(9997);
		conf.setBindAddress("127.0.0.1");
		conf.setUrlPattern("/*");

		return ThriftHTTPServer.getInstance(conf, processors, false);
	}
}
