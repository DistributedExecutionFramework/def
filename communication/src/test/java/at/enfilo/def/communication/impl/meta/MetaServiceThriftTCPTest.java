package at.enfilo.def.communication.impl.meta;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;

import java.util.Collections;

public class MetaServiceThriftTCPTest extends MetaServiceTest {
	@Override
	protected IServer getServer() throws ServerCreationException {
		DEFServerEndpointConfiguration conf = new DEFServerEndpointConfiguration();
		conf.setPort(9998);
		conf.setBindAddress("127.0.0.1");
		conf.setUrlPattern("/*");

		return ThriftTCPServer.getInstance(conf, Collections.emptyList(), false);
	}
}
