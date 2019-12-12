package at.enfilo.def.communication.impl.meta;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.meta.service.IMetaServiceClient;
import at.enfilo.def.communication.api.meta.service.MetaServiceClientFactory;
import at.enfilo.def.communication.exception.ServerCreationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class MetaServiceTest {
	private IServer server;
	private IMetaServiceClient client;
	private Thread serverThread;

	@Before
	public void setUp() throws Exception {
		server = getServer();
		serverThread = new Thread(server);
		serverThread.start();
		await().atMost(30, TimeUnit.SECONDS).until(server::isRunning);
		client = MetaServiceClientFactory.create(server.getServiceEndpoint());
	}

	@After
	public void teardown() throws Exception {
		client.close();
		server.close();
		serverThread.join();
	}

	protected abstract IServer getServer() throws ServerCreationException;

	@Test
	public void getVersion() throws Exception {
		String version = client.getVersion();
		assertNotNull(version);
		assertEquals("unknown", version);
	}

	@Test
	public void getTime() throws Exception {
		long timestamp = client.getTime();
		assertTrue(Math.abs(timestamp - System.currentTimeMillis()) < 2000);
	}
}
