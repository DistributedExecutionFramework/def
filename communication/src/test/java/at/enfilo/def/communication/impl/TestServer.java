package at.enfilo.def.communication.impl;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.misc.ServerStartup;
import at.enfilo.def.communication.misc.TestConfiguration;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class TestServer extends ServerStartup<TestConfiguration> {

	public static final String CONF_FILE = "testserver.yml";

	public List<ThriftProcessor> thriftProcessors;
	public List<IResource> resourceList;
	public TestConfiguration configuration;

	public TestServer(String confFile) {
		super(
				TestServer.class,
				TestConfiguration.class,
				confFile,
				LoggerFactory.getLogger(TestServer.class)
		);
		thriftProcessors = new LinkedList<>();
		thriftProcessors.add(new ThriftProcessor<>(
				DummyService.class,
				new DummyServiceImpl(),
				DummyService.Processor<DummyService.Iface>::new
		));
		resourceList = new LinkedList<>();
		resourceList.add(new DummyServiceImpl());
		configuration = new TestConfiguration();
	}

	@Override
	protected List<ThriftProcessor> getThriftProcessors() {
		return thriftProcessors;
	}

	@Override
	protected List<IResource> getWebResources() {
		return resourceList;
	}

	@Override
	protected TestConfiguration readConfiguration() {
		return configuration;
	}

	public TestConfiguration readConfigurationFromFile() {
		return super.readConfiguration();
	}
}
