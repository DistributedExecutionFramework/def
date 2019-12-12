package at.enfilo.def.reducer.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.reducer.api.thrift.ReducerResponseService;
import at.enfilo.def.reducer.api.thrift.ReducerService;
import at.enfilo.def.reducer.server.Reducer;

import java.util.LinkedList;
import java.util.List;

public class ReducerServiceThriftTest extends ReducerServiceTest {
	@Override
	protected IServer getServer(ReducerServiceController controller) throws Exception {
		ThriftProcessor reducerServiceProcessor = new ThriftProcessor<>(
				ReducerService.class.getCanonicalName(),
				new ReducerServiceImpl(controller, TicketRegistry.getInstance()),
				ReducerService.Processor<ReducerService.Iface>::new
		);
		ThriftProcessor reducerServiceResponseProcessor = new ThriftProcessor<>(
				ReducerResponseService.class.getCanonicalName(),
				new ReducerResponseServiceImpl(),
				ReducerResponseService.Processor<ReducerResponseService.Iface>::new
		);
		List<ThriftProcessor> processors = new LinkedList<>();
		processors.add(reducerServiceProcessor);
		processors.add(reducerServiceResponseProcessor);

		return ThriftTCPServer.getInstance(
				Reducer.getInstance().getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration(),
				processors
		);
	}
}
