package at.enfilo.def.manager.impl;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.communication.util.ServiceRegistry;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.manager.api.AuthServiceClientFactory;
import at.enfilo.def.manager.api.IAuthServiceClient;
import at.enfilo.def.transfer.dto.AuthDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public abstract class AuthServiceTest {

	private IServer server;
	private Thread serverThread;
	private IAuthServiceClient client;

	protected AuthController authController;

	@Before
	public void setUp() throws Exception {
		TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());
		authController = Mockito.mock(AuthController.class);

		this.server = getServer();
		serverThread = new Thread(server);
		serverThread.start();

		await().atMost(30, TimeUnit.SECONDS).until(server::isRunning);
		client = new AuthServiceClientFactory().createClient(server.getServiceEndpoint());
	}

	protected abstract IServer getServer() throws ServerCreationException;


	@After
	public void tearDown() throws Exception {
		client.close();
		server.close();
		serverThread.join();
		ServiceRegistry.getInstance().closeAll();
	}

	@Test
	public void getToken() throws Exception {
		String name = UUID.randomUUID().toString();
		String password = UUID.randomUUID().toString();
		AuthDTO auth = new AuthDTO();
		auth.setUserId(name);

		when(authController.getToken(name, password)).thenReturn(auth);

		Future<AuthDTO> futureAuth = client.getToken(name, password);
		await().atMost(10, TimeUnit.SECONDS).until(futureAuth::isDone);

		assertEquals(auth, futureAuth.get());
	}
}
