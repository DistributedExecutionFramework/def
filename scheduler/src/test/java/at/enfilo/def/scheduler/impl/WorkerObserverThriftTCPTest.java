package at.enfilo.def.scheduler.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.node.observer.api.thrift.NodeObserverService;
import at.enfilo.def.scheduler.server.Scheduler;

import java.util.LinkedList;
import java.util.List;

public class WorkerObserverThriftTCPTest extends WorkerObserverServiceTest {
	@Override
	protected IServer getServer() throws Exception {
		List<ThriftProcessor> thriftProcessors = new LinkedList<>();
		ThriftProcessor<NodeObserverServiceImpl> workerObserverServiceProcessor = new ThriftProcessor<>(
				NodeObserverService.class.getName(),
				new NodeObserverServiceImpl(schedulingController),
				NodeObserverService.Processor<NodeObserverService.Iface>::new
		);
		thriftProcessors.add(workerObserverServiceProcessor);

		return ThriftTCPServer.getInstance(
				Scheduler.getInstance().getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration(),
				thriftProcessors
		);
	}
}
