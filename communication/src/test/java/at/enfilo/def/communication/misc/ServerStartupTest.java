package at.enfilo.def.communication.misc;

import at.enfilo.def.communication.api.common.client.RESTClient;
import at.enfilo.def.communication.api.common.client.ThriftHTTPClient;
import at.enfilo.def.communication.api.common.client.ThriftTCPClient;
import at.enfilo.def.communication.impl.DummyService;
import at.enfilo.def.communication.impl.DummyServiceImpl;
import at.enfilo.def.communication.impl.IDummyService;
import at.enfilo.def.communication.impl.TestServer;
import at.enfilo.def.communication.rest.RESTServer;
import at.enfilo.def.communication.thrift.http.ThriftHTTPServer;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.communication.util.ServiceRegistry;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;


public class ServerStartupTest {
	private static final int THRIFT_TCP_PORT = 9991;
	private static final int THRIFT_HTTP_PORT = 9992;
	private static final int REST_PORT = 9993;

	private TestServer testServer;

	@Before
	public void setUp() throws Exception {
		testServer = new TestServer(TestServer.CONF_FILE);
	}


	@Test
	public void readConfigurationFromFile() throws Exception {
		TestConfiguration conf = testServer.readConfigurationFromFile();
		assertNotNull(conf);
		assertTrue(conf.getTicketServiceConfiguration().isEnabled());
		assertTrue(conf.getServerHolderConfiguration().getRESTConfiguration().isEnabled());
		assertTrue(conf.getServerHolderConfiguration().getThriftTCPConfiguration().isEnabled());
		assertTrue(conf.getServerHolderConfiguration().getThriftHTTPConfiguration().isEnabled());
	}

	@Test
	public void startupThriftTCPService() throws Exception {
		ServiceRegistry registry = ServiceRegistry.getInstance();
		assertEquals(0, registry.registeredServices());

		// Setup Configuration
		testServer.configuration.getServerHolderConfiguration().setRESTConfiguration(null);
		testServer.configuration.getServerHolderConfiguration().setThriftHTTPConfiguration(null);
		testServer.configuration.getServerHolderConfiguration().setThriftTCPConfiguration(new DEFServerEndpointConfiguration());
		testServer.configuration.getServerHolderConfiguration().getThriftTCPConfiguration().setEnabled(true);
		testServer.configuration.getServerHolderConfiguration().getThriftTCPConfiguration().setPort(THRIFT_TCP_PORT);

		// Start services
		testServer.startServices();
		assertEquals(1, registry.registeredServices());
		assertTrue(registry.hasServiceInstanceOf(ThriftTCPServer.class));

		DEFServerEndpointConfiguration thriftTCPConfig = testServer.configuration.getServerHolderConfiguration().getThriftTCPConfiguration();
		await().atMost(10, TimeUnit.SECONDS).until(testServer.getThriftTCPServer(thriftTCPConfig)::isRunning);

		// Ping service
		ThriftTCPClient<DummyService.Client> client = new ThriftTCPClient<>(
				testServer.getThriftTCPServer(thriftTCPConfig).getServiceEndpoint(),
				DummyService.class,
				DummyService.Client::new
		);
		String response = client.execute(DummyService.Client::ping);
		assertEquals(DummyServiceImpl.RESPONSE, response);

		// Stop all services
		registry.closeAll();
		assertEquals(0, registry.registeredServices());
	}


	@Test
	public void startupThriftHTTPService() throws Exception {
		ServiceRegistry registry = ServiceRegistry.getInstance();
		assertEquals(0, registry.registeredServices());

		// Setup Configuration
		testServer.configuration.getServerHolderConfiguration().setRESTConfiguration(null);
		testServer.configuration.getServerHolderConfiguration().setThriftTCPConfiguration(null);
		testServer.configuration.getServerHolderConfiguration().setThriftHTTPConfiguration(new DEFServerEndpointConfiguration());
		testServer.configuration.getServerHolderConfiguration().getThriftHTTPConfiguration().setEnabled(true);
		testServer.configuration.getServerHolderConfiguration().getThriftHTTPConfiguration().setPort(THRIFT_HTTP_PORT);

		// Start services
		testServer.startServices();
		assertEquals(1, registry.registeredServices());
		assertTrue(registry.hasServiceInstanceOf(ThriftHTTPServer.class));

		DEFServerEndpointConfiguration thriftHTTPConfig = testServer.configuration.getServerHolderConfiguration().getThriftHTTPConfiguration();
		await().atMost(10, TimeUnit.SECONDS).until(testServer.getThriftHTTPServer(thriftHTTPConfig)::isRunning);

		// Ping service
		ThriftHTTPClient<DummyService.Client> client = new ThriftHTTPClient<>(
				testServer.getThriftHTTPServer(thriftHTTPConfig).getServiceEndpoint(),
				DummyService.class,
				DummyService.Client::new
		);
		String response = client.execute(DummyService.Client::ping);
		assertEquals(DummyServiceImpl.RESPONSE, response);

		// Stop all services
		registry.closeAll();
		assertEquals(0, registry.registeredServices());
	}

	@Test
	public void startupRestService() throws Exception {
		ServiceRegistry registry = ServiceRegistry.getInstance();
		assertEquals(0, registry.registeredServices());

		// Setup Configuration
		testServer.configuration.getServerHolderConfiguration().setThriftHTTPConfiguration(null);
		testServer.configuration.getServerHolderConfiguration().setThriftTCPConfiguration(null);
		testServer.configuration.getServerHolderConfiguration().setRESTConfiguration(new DEFServerEndpointConfiguration());
		testServer.configuration.getServerHolderConfiguration().getRESTConfiguration().setEnabled(true);
		testServer.configuration.getServerHolderConfiguration().getRESTConfiguration().setPort(REST_PORT);

		// Start services
		testServer.startServices();
		assertEquals(1, registry.registeredServices());
		assertTrue(registry.hasServiceInstanceOf(RESTServer.class));

		DEFServerEndpointConfiguration restConfig = testServer.configuration.getServerHolderConfiguration().getRESTConfiguration();
		await().atMost(10, TimeUnit.SECONDS).until(testServer.getRESTServer(restConfig)::isRunning);

		// Ping service
		RESTClient<IDummyService> client = new RESTClient<>(
				testServer.getRESTServer(restConfig).getServiceEndpoint(),
				IDummyService.class
		);
		String response = client.execute(IDummyService::ping);
		assertEquals(DummyServiceImpl.RESPONSE, response);

		// Stop all services
		registry.closeAll();
		assertEquals(0, registry.registeredServices());
	}

	@Test
	public void startAllServices() throws Exception {
		ServiceRegistry registry = ServiceRegistry.getInstance();
		assertEquals(0, registry.registeredServices());

		// Setup Configuration
		testServer.configuration.getServerHolderConfiguration().setRESTConfiguration(new DEFServerEndpointConfiguration());
		testServer.configuration.getServerHolderConfiguration().getRESTConfiguration().setEnabled(true);
		testServer.configuration.getServerHolderConfiguration().getRESTConfiguration().setPort(REST_PORT);
		testServer.configuration.getServerHolderConfiguration().setThriftHTTPConfiguration(new DEFServerEndpointConfiguration());
		testServer.configuration.getServerHolderConfiguration().getThriftHTTPConfiguration().setEnabled(true);
		testServer.configuration.getServerHolderConfiguration().getThriftHTTPConfiguration().setPort(THRIFT_HTTP_PORT);
		testServer.configuration.getServerHolderConfiguration().setThriftTCPConfiguration(new DEFServerEndpointConfiguration());
		testServer.configuration.getServerHolderConfiguration().getThriftTCPConfiguration().setEnabled(true);
		testServer.configuration.getServerHolderConfiguration().getThriftTCPConfiguration().setPort(THRIFT_TCP_PORT);

		// Start services
		testServer.startServices();
		assertEquals(3, registry.registeredServices());
		assertTrue(registry.hasServiceInstanceOf(RESTServer.class));
		assertTrue(registry.hasServiceInstanceOf(ThriftHTTPServer.class));
		assertTrue(registry.hasServiceInstanceOf(ThriftTCPServer.class));

		DEFServerEndpointConfiguration thriftTCPConfig = testServer.configuration.getServerHolderConfiguration().getThriftTCPConfiguration();
		DEFServerEndpointConfiguration thriftHTTPConfig = testServer.configuration.getServerHolderConfiguration().getThriftHTTPConfiguration();
		DEFServerEndpointConfiguration restConfig = testServer.configuration.getServerHolderConfiguration().getRESTConfiguration();
		await().atMost(10, TimeUnit.SECONDS).until(testServer.getThriftTCPServer(thriftTCPConfig)::isRunning);
		await().atMost(10, TimeUnit.SECONDS).until(testServer.getThriftHTTPServer(thriftHTTPConfig)::isRunning);
		await().atMost(10, TimeUnit.SECONDS).until(testServer.getRESTServer(restConfig)::isRunning);

		// Stop all services
		registry.closeAll();
		assertEquals(0, registry.registeredServices());
	}

	@Test
	public void readDefaultConf() throws Exception {
		TestServer ts = new TestServer("wrong-config-file");
		TestConfiguration conf = ts.readConfigurationFromFile();
		assertNotNull(conf);
	}
}
