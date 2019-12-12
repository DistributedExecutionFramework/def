package at.enfilo.def.execlogic.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.rest.RESTServer;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

public class ExecLogicServiceRESTTest extends ExecLogicServiceTest {
	@Override
	protected IServer getServer() throws Exception {
		List<IResource> webResources = new LinkedList<>();
		webResources.add(new ExecLogicServiceImpl(execLogicController));
		webResources.add(new ExecLogicResponseServiceImpl());

		DEFServerEndpointConfiguration restConfig = new DEFServerEndpointConfiguration();
		restConfig.setPort(44111);
		restConfig.setBindAddress(InetAddress.getLoopbackAddress().getHostAddress());
		restConfig.setUrlPattern("/*");

		return RESTServer.getInstance(
				restConfig,
				webResources
		);
	}

}
