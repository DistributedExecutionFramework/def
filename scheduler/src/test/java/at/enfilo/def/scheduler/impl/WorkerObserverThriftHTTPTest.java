package at.enfilo.def.scheduler.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.http.ThriftHTTPServer;
import at.enfilo.def.node.observer.api.thrift.NodeObserverService;
import at.enfilo.def.scheduler.server.Scheduler;

import java.util.LinkedList;
import java.util.List;

public class WorkerObserverThriftHTTPTest extends WorkerObserverServiceTest {
	@Override
	protected IServer getServer() throws Exception {
		List<ThriftProcessor> thriftProcessors = new LinkedList<>();
		ThriftProcessor<NodeObserverServiceImpl> workerObserverServiceProcessor = new ThriftProcessor<>(
				NodeObserverService.class.getName(),
				new NodeObserverServiceImpl(schedulingController),
				NodeObserverService.Processor<NodeObserverService.Iface>::new
		);
		thriftProcessors.add(workerObserverServiceProcessor);

		return ThriftHTTPServer.getInstance(
				Scheduler.getInstance().getConfiguration().getServerHolderConfiguration().getThriftHTTPConfiguration(),
				thriftProcessors
		);
	}
}
