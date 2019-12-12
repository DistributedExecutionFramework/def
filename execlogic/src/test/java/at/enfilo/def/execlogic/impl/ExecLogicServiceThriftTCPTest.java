package at.enfilo.def.execlogic.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;
import at.enfilo.def.execlogic.api.thrift.ExecLogicResponseService;
import at.enfilo.def.execlogic.api.thrift.ExecLogicService;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

public class ExecLogicServiceThriftTCPTest extends ExecLogicServiceTest {
	@Override
	protected IServer getServer() throws Exception {
		List<ThriftProcessor> thriftProcessors = new LinkedList<>();
		ThriftProcessor<ExecLogicServiceImpl> execLogicServiceProcessor = new ThriftProcessor<>(
				ExecLogicService.class.getName(),
				new ExecLogicServiceImpl(execLogicController),
				ExecLogicService.Processor<ExecLogicService.Iface>::new
		);
		ThriftProcessor<ExecLogicResponseServiceImpl> execLogicResponseServiceProcessor = new ThriftProcessor<>(
				ExecLogicResponseService.class.getName(),
				new ExecLogicResponseServiceImpl(),
				ExecLogicResponseService.Processor<ExecLogicResponseService.Iface>::new
		);
		thriftProcessors.add(execLogicServiceProcessor);
		thriftProcessors.add(execLogicResponseServiceProcessor);

		DEFServerEndpointConfiguration thriftTCPConfig= new DEFServerEndpointConfiguration();
		thriftTCPConfig.setPort(40002);
		thriftTCPConfig.setBindAddress(InetAddress.getLoopbackAddress().getHostAddress());

		return ThriftTCPServer.getInstance(
				thriftTCPConfig,
				thriftProcessors
		);
	}

}
