package at.enfilo.def.scheduler.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.scheduler.api.thrift.SchedulerResponseService;
import at.enfilo.def.scheduler.api.thrift.SchedulerService;
import at.enfilo.def.scheduler.server.Scheduler;

import java.util.LinkedList;
import java.util.List;

public class SchedulerServiceThriftTCPTest extends SchedulerServiceTest {
	@Override
	protected IServer getServer() throws Exception {
		List<ThriftProcessor> thriftProcessors = new LinkedList<>();
		ThriftProcessor<SchedulerServiceImpl> schedulerServiceProcessor = new ThriftProcessor<>(
				SchedulerService.class.getName(),
				new SchedulerServiceImpl(schedulingController),
				SchedulerService.Processor<SchedulerService.Iface>::new
		);
		thriftProcessors.add(schedulerServiceProcessor);
		ThriftProcessor<SchedulerResponseServiceImpl> schedulerResponseServiceProcessor = new ThriftProcessor<>(
				SchedulerResponseService.class.getName(),
				new SchedulerResponseServiceImpl(),
				SchedulerResponseService.Processor<SchedulerResponseService.Iface>::new
		);
		thriftProcessors.add(schedulerResponseServiceProcessor);

		return ThriftTCPServer.getInstance(
				Scheduler.getInstance().getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration(),
				thriftProcessors
		);
	}
}
