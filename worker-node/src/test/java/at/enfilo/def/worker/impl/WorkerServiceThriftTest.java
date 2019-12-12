package at.enfilo.def.worker.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.impl.ticket.TicketRegistry;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.worker.api.thrift.WorkerResponseService;
import at.enfilo.def.worker.api.thrift.WorkerService;
import at.enfilo.def.worker.server.Worker;

import java.util.LinkedList;
import java.util.List;

public class WorkerServiceThriftTest extends WorkerServiceTest {

	private static final String REQUEST_SERVICE_NAME = WorkerService.class.getName();
	private static final String RESPONSE_SERVICE_NAME = WorkerResponseService.class.getName();


	@Override
	protected IServer getServer(WorkerServiceController controller) throws Exception {

		ThriftProcessor workerServiceProcessor = new ThriftProcessor<>(
				REQUEST_SERVICE_NAME,
				new WorkerServiceImpl(controller, TicketRegistry.getInstance()),
				WorkerService.Processor<WorkerService.Iface>::new
		);
		ThriftProcessor workerServiceResponseProcessor = new ThriftProcessor<>(
				RESPONSE_SERVICE_NAME,
				new WorkerResponseServiceImpl(),
				WorkerResponseService.Processor<WorkerResponseService.Iface>::new
		);
		List<ThriftProcessor> processors = new LinkedList<>();
		processors.add(workerServiceProcessor);
		processors.add(workerServiceResponseProcessor);

		return ThriftTCPServer.getInstance(
				Worker.getInstance().getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration(),
				processors
		);
	}


}
