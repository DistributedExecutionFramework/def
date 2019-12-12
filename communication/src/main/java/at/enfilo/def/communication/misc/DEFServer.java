package at.enfilo.def.communication.misc;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ServerShutdownException;
import at.enfilo.def.communication.exception.ServerStartupException;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.ws.rs.Path;
import java.io.IOException;
import java.util.Observable;

/**
 * Created by mase on 19.09.2016.
 */
public abstract class DEFServer extends Observable implements IServer {

	private static final String DEFAULT_PATTERN_PLACEHOLDER = "*";
	private static final String DEFAULT_PATH_PREFIX = "/";
	private static final String DEFAULT_PATH_REPLACEMENT_VALUE = "";

	private final Logger logger;

	private final String urlPattern;
	private final ServiceEndpointDTO serviceEndpoint;

	/**
	 * Protected constructor for concrete implementations
	 *
	 * @param protocol     - service protocol
	 * @param serverConfig - specific configuration
	 * @param logger       - logger
	 */
	protected DEFServer(@Nonnull Protocol protocol, @Nonnull DEFServerEndpointConfiguration serverConfig, @Nonnull Logger logger) {
		// Registering default logger.
		this.logger = logger;

		this.urlPattern = serverConfig.getUrlPattern();

		// Assembling service endpoint information.
		serviceEndpoint = new ServiceEndpointDTO(
			serverConfig.getBindAddress(),
			serverConfig.getPort(),
			protocol
		);
		serviceEndpoint.setPathPrefix(urlPattern);
	}

	protected abstract void doStartServer() throws ServerStartupException;

	protected abstract void doStopServer() throws ServerShutdownException;

	protected String getUrlPattern() {
		return urlPattern;
	}

	/**
	 * Convenience method that forges path by using given pattern and replace value.
	 *
	 * @param urlPattern pattern that specifies how path should look like.
	 * @param value      value that will be used to replace default placeholder occurrences in pattern.
	 * @return String forged path.
	 */
	protected String forgePath(String urlPattern, String value) {
		if (value != null) {
			String path = value.startsWith(DEFAULT_PATH_PREFIX) ? value.substring(1) : value;
			return urlPattern.replace(DEFAULT_PATTERN_PLACEHOLDER, path);
		}
		return urlPattern.replace(DEFAULT_PATTERN_PLACEHOLDER, DEFAULT_PATH_REPLACEMENT_VALUE);
	}

	protected String forgePath(String urlPattern, Class<? extends IResource> resourceClass) {
		// Searching in the first level of interfaces for the path.
		Class<?>[] interfaces = resourceClass.getInterfaces();
		for (Class<?> currentInterface : interfaces) {
			Path pathAnnotation = currentInterface.getAnnotation(Path.class);
			if (pathAnnotation != null) {
				return forgePath(urlPattern, pathAnnotation.value());
			}
		}
		return forgePath(urlPattern, DEFAULT_PATTERN_PLACEHOLDER);
	}

	@Override
	public ServiceEndpointDTO getServiceEndpoint() {
		return serviceEndpoint;
	}

	@Override
	public void run() {
		try {
			if (!isRunning()) {
				logger.info(
						"Starting {} server. Host: {}, Port: {}.",
						serviceEndpoint.getProtocol(),
						serviceEndpoint.getHost(),
						serviceEndpoint.getPort()
				);
				doStartServer();
			} else {
				logger.warn(
						"Server ({}, {}, {}) already running, ignore start request.",
						serviceEndpoint.getHost(),
						serviceEndpoint.getPort(),
						serviceEndpoint.getProtocol()
				);
			}
			setChanged();
			notifyObservers();

		} catch (ServerStartupException e) {
			logger.error("Error while start server.", e);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			if (isRunning()) {
				logger.info(
						"Closing {} server. Host: {}, Port: {}.",
						serviceEndpoint.getProtocol(),
						serviceEndpoint.getHost(),
						serviceEndpoint.getPort()
				);
				doStopServer();
			} else {
				logger.warn(
						"Server ({}, {}, {}) not running, ignore stop request.",
						serviceEndpoint.getHost(),
						serviceEndpoint.getPort(),
						serviceEndpoint.getProtocol()
				);
			}

			setChanged();
			notifyObservers();

		} catch (ServerShutdownException e) {
			logger.error("Error while stopping server.", e);
			throw new IOException(e);
		}
	}
}
