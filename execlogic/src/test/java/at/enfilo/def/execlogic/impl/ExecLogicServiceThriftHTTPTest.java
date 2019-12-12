package at.enfilo.def.execlogic.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.http.ThriftHTTPServer;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;
import at.enfilo.def.execlogic.api.thrift.ExecLogicResponseService;
import at.enfilo.def.execlogic.api.thrift.ExecLogicService;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

public class ExecLogicServiceThriftHTTPTest extends ExecLogicServiceTest {
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

		DEFServerEndpointConfiguration thriftHTTPConfig = new DEFServerEndpointConfiguration();
		thriftHTTPConfig.setPort(40001);
		thriftHTTPConfig.setBindAddress(InetAddress.getLoopbackAddress().getHostAddress());
		thriftHTTPConfig.setUrlPattern("/*");

		return ThriftHTTPServer.getInstance(
				thriftHTTPConfig,
				thriftProcessors
		);
	}

}
