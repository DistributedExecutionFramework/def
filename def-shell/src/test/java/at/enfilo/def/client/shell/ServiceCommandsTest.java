package at.enfilo.def.client.shell;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;
import org.junit.Test;
import org.springframework.shell.core.CommandResult;

import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static at.enfilo.def.client.shell.Constants.*;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServiceCommandsTest extends ShellBaseTest {
	@Test
	public void switchDirect() throws Exception {
		Random rnd = new Random();
		String host = UUID.randomUUID().toString();
		Integer port = rnd.nextInt();
		Protocol protocol = Protocol.THRIFT_TCP;
		String cmd = String.format("%s --%s %s --%s %s --%s %d --%s %s",
				CMD_SERVICE_SWITCH,
				OPT_SERVICE, Service.CLUSTER,
				OPT_HOST, host,
				OPT_PORT, port,
				OPT_PROTOCOL, protocol
		);
		CommandResult result = shell.executeCommand(cmd);
		assertTrue(result.isSuccess());
		assertEquals(Service.CLUSTER, session.getActiveService());
		assertEquals(host, session.getActiveEndpoint().getHost());
		assertEquals(protocol, session.getActiveEndpoint().getProtocol());
		assertEquals(port.intValue(), session.getActiveEndpoint().getPort());
	}

	@Test
	public void switchEndpoint() throws Exception {
		Random rnd = new Random();
		String host = UUID.randomUUID().toString();
		int port = rnd.nextInt();
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(
				host,
				port,
				Protocol.REST
		);
		String name = UUID.randomUUID().toString();
		objects.getObjectMap().put(name, endpoint);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s",
						CMD_SERVICE_SWITCH,
						OPT_SERVICE, Service.MANAGER,
						OPT_SERVICE_ENDPOINT, name
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(Service.MANAGER, session.getActiveService());
		assertEquals(host, session.getActiveEndpoint().getHost());
		assertEquals(port, session.getActiveEndpoint().getPort());
	}

	@Test
	public void switchWrongArgs() throws Exception {
		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s",
						CMD_SERVICE_SWITCH,
						OPT_SERVICE, Service.MANAGER
				)
		);
		assertEquals(MESSAGE_SERVICE_ENDPOINT_OR_DIRECT, result.getResult().toString());
	}

	@Test
	public void versionDirect() throws Exception {
		String host = "localhost";
		Integer port = 12345;
		Protocol protocol = Protocol.THRIFT_TCP;
		String cmd = String.format("%s --%s %s --%s %s --%s %d --%s %s",
				CMD_SERVICE_VERSION,
				OPT_SERVICE, Service.CLUSTER,
				OPT_HOST, host,
				OPT_PORT, port,
				OPT_PROTOCOL, protocol
		);

		IServer server = startServer(host, port);

		CommandResult result = shell.executeCommand(cmd);
		assertTrue(result.isSuccess());
		assertEquals(String.format(MESSAGE_SERVICE_VERSION, Service.CLUSTER, "unknown"), result.getResult().toString());

		server.close();
	}

	@Test
	public void versionEndpoint() throws Exception {
		String host = "localhost";
		int port = 12345;
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(
				host,
				port,
				Protocol.THRIFT_TCP
		);
		String name = UUID.randomUUID().toString();
		objects.getObjectMap().put(name, endpoint);

		IServer server = startServer(host, port);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s",
						CMD_SERVICE_VERSION,
						OPT_SERVICE, Service.MANAGER,
						OPT_SERVICE_ENDPOINT, name
				)
		);
		assertTrue(result.isSuccess());
		assertEquals(String.format(MESSAGE_SERVICE_VERSION, Service.MANAGER, "unknown"), result.getResult().toString());

		server.close();
	}

	@Test
	public void versionWrongArgs() throws Exception {
		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s",
						CMD_SERVICE_VERSION,
						OPT_SERVICE, Service.MANAGER
				)
		);
		assertEquals(MESSAGE_SERVICE_ENDPOINT_OR_DIRECT, result.getResult().toString());
	}

	@Test
	public void timeDirect() throws Exception {
		String host = "localhost";
		Integer port = 12345;
		Protocol protocol = Protocol.THRIFT_TCP;
		String cmd = String.format("%s --%s %s --%s %s --%s %d --%s %s",
				CMD_SERVICE_TIME,
				OPT_SERVICE, Service.CLUSTER,
				OPT_HOST, host,
				OPT_PORT, port,
				OPT_PROTOCOL, protocol
		);

		IServer server = startServer(host, port);

		CommandResult result = shell.executeCommand(cmd);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(
				String.format(MESSAGE_SERVICE_TIME, Service.CLUSTER, "")
		));

		server.close();
	}

	@Test
	public void timeEndpoint() throws Exception {
		String host = "localhost";
		int port = 12345;
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(
				host,
				port,
				Protocol.THRIFT_TCP
		);
		String name = UUID.randomUUID().toString();
		objects.getObjectMap().put(name, endpoint);

		IServer server = startServer(host, port);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s",
						CMD_SERVICE_TIME,
						OPT_SERVICE, Service.MANAGER,
						OPT_SERVICE_ENDPOINT, name
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(
				String.format(MESSAGE_SERVICE_TIME, Service.MANAGER, "")
		));

		server.close();
	}

	@Test
	public void timeWrongArgs() throws Exception {
		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s",
						CMD_SERVICE_TIME,
						OPT_SERVICE, Service.MANAGER
				)
		);
		assertEquals(MESSAGE_SERVICE_ENDPOINT_OR_DIRECT, result.getResult().toString());
	}

	@Test
	public void pingDirect() throws Exception {
		String host = "localhost";
		Integer port = 12345;
		Protocol protocol = Protocol.THRIFT_TCP;
		String cmd = String.format("%s --%s %s --%s %s --%s %d --%s %s",
				CMD_SERVICE_PING,
				OPT_SERVICE, Service.CLUSTER,
				OPT_HOST, host,
				OPT_PORT, port,
				OPT_PROTOCOL, protocol
		);

		IServer server = startServer(host, port);

		CommandResult result = shell.executeCommand(cmd);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().startsWith(
				String.format(MESSAGE_SERVICE_PING, Service.CLUSTER, 1, 1, 1).substring(0, 30)
		));

		server.close();
	}

	@Test
	public void pingEndpoint() throws Exception {
		String host = "localhost";
		int port = 12345;
		ServiceEndpointDTO endpoint = new ServiceEndpointDTO(
				host,
				port,
				Protocol.THRIFT_TCP
		);
		String name = UUID.randomUUID().toString();
		objects.getObjectMap().put(name, endpoint);

		IServer server = startServer(host, port);

		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s --%s %s",
						CMD_SERVICE_PING,
						OPT_SERVICE, Service.MANAGER,
						OPT_SERVICE_ENDPOINT, name
				)
		);
		assertTrue(result.isSuccess());
		assertTrue(result.getResult().toString().contains(
				String.format(MESSAGE_SERVICE_PING, Service.MANAGER, 1, 1, 1).substring(0, 30)
		));

		server.close();
	}

	@Test
	public void pingWrongArgs() throws Exception {
		CommandResult result = shell.executeCommand(
				String.format("%s --%s %s",
						CMD_SERVICE_PING,
						OPT_SERVICE, Service.MANAGER
				)
		);
		assertEquals(MESSAGE_SERVICE_ENDPOINT_OR_DIRECT, result.getResult().toString());
	}

	private IServer startServer(String host, int port) throws ServerCreationException {
		DEFServerEndpointConfiguration conf = new DEFServerEndpointConfiguration();
		conf.setPort(port);
		conf.setBindAddress(host);
		conf.setUrlPattern("/*");

		IServer server = ThriftTCPServer.getInstance(conf, Collections.emptyList(), false);
		new Thread(server).start();
		await().atMost(10, TimeUnit.SECONDS).until(server::isRunning);
		return server;
	}
}
