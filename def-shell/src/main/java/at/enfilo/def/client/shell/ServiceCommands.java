package at.enfilo.def.client.shell;

import at.enfilo.def.communication.api.meta.service.IMetaServiceClient;
import at.enfilo.def.communication.api.meta.service.MetaServiceClientFactory;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static at.enfilo.def.client.shell.Constants.*;


@Component
public class ServiceCommands implements CommandMarker {
	@Autowired
	private DEFShellSession session;
	@Autowired
	private ObjectCommands objects;

	@CliCommand(value = CMD_SERVICE_SWITCH, help = "Switch context to specified service")
	public String switchService(
			@CliOption(key = OPT_SERVICE, mandatory = true, help = "Service") final Service service,
			@CliOption(key = OPT_HOST, help = "Service host") final String host,
			@CliOption(key = OPT_PORT, help = "Service port") final Integer port,
			@CliOption(key = OPT_PROTOCOL, help = "Service protocol") final Protocol protocol,
			@CliOption(key = OPT_URL_PATTERN, unspecifiedDefaultValue = "/*", specifiedDefaultValue = "/*", help = "Service URL pattern") final String pattern,
			@CliOption(key = OPT_SERVICE_ENDPOINT, help = "ServiceEndpoint Object Name") final String object
	) throws ClientCreationException {

		ServiceEndpointDTO endpoint;
		if (object != null) {
			endpoint = objects.getObject(object, ServiceEndpointDTO.class);
		} else if (host != null && port != null && protocol != null) {
			endpoint = new ServiceEndpointDTO(host, port, protocol);
			endpoint.setPathPrefix(pattern);
		} else {
			return MESSAGE_SERVICE_ENDPOINT_OR_DIRECT;
		}

		session.switchToService(service, endpoint);
		return String.format(
				MESSAGE_SERVICE_SWITCHED,
				service,
				endpoint.getHost(),
				endpoint.getPort(),
				endpoint.getPathPrefix()
		);
	}


	@CliCommand(value = CMD_SERVICE_VERSION, help = "Request version of specified service")
	public String version(
			@CliOption(key = OPT_SERVICE, mandatory = true, help = "Service") final Service service,
			@CliOption(key = OPT_HOST, help = "Service host") final String host,
			@CliOption(key = OPT_PORT, help = "Service port") final Integer port,
			@CliOption(key = OPT_PROTOCOL, help = "Service protocol") final Protocol protocol,
			@CliOption(key = OPT_URL_PATTERN, unspecifiedDefaultValue = "/*", specifiedDefaultValue = "/*", help = "Service URL pattern") final String pattern,
			@CliOption(key = OPT_SERVICE_ENDPOINT, help = "ServiceEndpoint Object Name") final String object
	) throws ClientCreationException, ClientCommunicationException {

		ServiceEndpointDTO endpoint;
		if (object != null) {
			endpoint = objects.getObject(object, ServiceEndpointDTO.class);
		} else if (host != null && port != null && protocol != null) {
			endpoint = new ServiceEndpointDTO(host, port, protocol);
			endpoint.setPathPrefix(pattern);
		} else {
			return MESSAGE_SERVICE_ENDPOINT_OR_DIRECT;
		}

		IMetaServiceClient client = MetaServiceClientFactory.create(endpoint);

		return String.format(
				MESSAGE_SERVICE_VERSION,
				service,
				client.getVersion()
		);
	}


	@CliCommand(value = CMD_SERVICE_TIME, help = "Request time of specified service host")
	public String time(
			@CliOption(key = OPT_SERVICE, mandatory = true, help = "Service") final Service service,
			@CliOption(key = OPT_HOST, help = "Service host") final String host,
			@CliOption(key = OPT_PORT, help = "Service port") final Integer port,
			@CliOption(key = OPT_PROTOCOL, help = "Service protocol") final Protocol protocol,
			@CliOption(key = OPT_URL_PATTERN, unspecifiedDefaultValue = "/*", specifiedDefaultValue = "/*", help = "Service URL pattern") final String pattern,
			@CliOption(key = OPT_SERVICE_ENDPOINT, help = "ServiceEndpoint Object Name") final String object
	) throws ClientCreationException, ClientCommunicationException {

		ServiceEndpointDTO endpoint;
		if (object != null) {
			endpoint = objects.getObject(object, ServiceEndpointDTO.class);
		} else if (host != null && port != null && protocol != null) {
			endpoint = new ServiceEndpointDTO(host, port, protocol);
			endpoint.setPathPrefix(pattern);
		} else {
			return MESSAGE_SERVICE_ENDPOINT_OR_DIRECT;
		}

		IMetaServiceClient client = MetaServiceClientFactory.create(endpoint);

		return String.format(
				MESSAGE_SERVICE_TIME,
				service,
				Instant.ofEpochMilli(client.getTime()).toString()
		);
	}


	@CliCommand(value = CMD_SERVICE_PING, help = "Ping specified service (fetch version and take round-trip-time)")
	public String ping(
			@CliOption(key = OPT_SERVICE, mandatory = true, help = "Service") final Service service,
			@CliOption(key = OPT_HOST, help = "Service host") final String host,
			@CliOption(key = OPT_PORT, help = "Service port") final Integer port,
			@CliOption(key = OPT_PROTOCOL, help = "Service protocol") final Protocol protocol,
			@CliOption(key = OPT_URL_PATTERN, unspecifiedDefaultValue = "/*", specifiedDefaultValue = "/*", help = "Service URL pattern") final String pattern,
			@CliOption(key = OPT_SERVICE_ENDPOINT, help = "ServiceEndpoint Object Name") final String object
	) throws ClientCreationException {

		ServiceEndpointDTO endpoint;
		if (object != null) {
			endpoint = objects.getObject(object, ServiceEndpointDTO.class);
		} else if (host != null && port != null && protocol != null) {
			endpoint = new ServiceEndpointDTO(host, port, protocol);
			endpoint.setPathPrefix(pattern);
		} else {
			return MESSAGE_SERVICE_ENDPOINT_OR_DIRECT;
		}

		IMetaServiceClient client = MetaServiceClientFactory.create(endpoint);

		// Ping 3x
		long[] rtts = new long[3];
		try {
			for (int i = 0; i < 3; i++) {
				long start = System.currentTimeMillis();
				client.getVersion();
				long end = System.currentTimeMillis();
				rtts[i] = end - start;
			}
		} catch (ClientCommunicationException e) {
			return String.format(MESSAGE_SERVICE_NOT_AVAIL, e.getMessage());
		}

		return String.format(
				MESSAGE_SERVICE_PING,
				service,
				rtts[0], rtts[1], rtts[2]
		);
	}
}
