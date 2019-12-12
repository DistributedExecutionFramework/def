package at.enfilo.def.communication.misc;

import at.enfilo.def.communication.api.common.server.IServer;
import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.communication.rest.RESTServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.http.ThriftHTTPServer;
import at.enfilo.def.communication.thrift.tcp.ThriftTCPServer;
import at.enfilo.def.communication.util.ServiceRegistry;
import at.enfilo.def.config.server.core.DEFRootConfiguration;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;
import at.enfilo.def.config.server.core.DEFServerHolderConfiguration;
import at.enfilo.def.config.util.ConfigReader;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Base class for a server. (MicroService)
 * Includes logic to parse configuration and startup all configured services.
 *
 * @param <T> - type of configuration
 */
public abstract class ServerStartup<T extends DEFRootConfiguration> {

	private final Logger logger;

    private final Class serverClass;
    private final Class<T> configClass;
    private final String configPath;

    private IServer restServer;
    private IServer thriftHTTPServer;
    private IServer thriftTCPServer;
	private T configuration;

	public ServerStartup(Class serverClass, Class<T> configClass, String configFile, Logger logger) {
		this.serverClass = serverClass;
		this.configClass = configClass;
		this.configPath = configFile;
        this.logger = logger;
	}

	/**
	 * Reads configuration file and returns the configuration.
	 * If configuration cannot be read, create a default configuration.
	 *
	 * @return Configuration instance
	 */
	protected T readConfiguration() {
		if (configuration == null) {
			try {
				logger.info("Try to read configuration from \"{}\".", configPath);
				configuration = ConfigReader.readConfiguration(
                    configPath, serverClass, configClass
				);
			} catch (IOException e) {
				logger.warn("Reading configuration from file failed.", e);
				logger.warn("Creating default configuration.");
				try {
					configuration = configClass.newInstance();
				} catch (InstantiationException | IllegalAccessException ex2) {
					logger.error("Error while creating new configuration instance.", ex2);
				}
			}
		}
		return configuration;
	}

	/**
	 * Returns RESTServer as singleton instance.
	 *
	 * @param config - configuration for server
	 * @return singleton instance
	 * @throws ServerCreationException
	 */
	public IServer getRESTServer(DEFServerEndpointConfiguration config) throws ServerCreationException {
		if (restServer == null) {
			restServer = RESTServer.getInstance(config, getWebResources());
		}
		return restServer;
	}

	/**
	 * Returns ThriftTCPServer as singleton instance.
	 *
	 * @param config - configuration for server
	 * @return singleton instance
	 * @throws ServerCreationException
	 */
	public IServer getThriftTCPServer(DEFServerEndpointConfiguration config) throws ServerCreationException {
		if (thriftTCPServer == null) {
			thriftTCPServer = ThriftTCPServer.getInstance(config, getThriftProcessors());
		}
		return thriftTCPServer;
	}

	/**
	 * Returns ThriftHTTPServer as singleton instance.
	 *
	 * @param config - configuration for server
	 * @return singleton instance
	 * @throws ServerCreationException
	 */
	public IServer getThriftHTTPServer(DEFServerEndpointConfiguration config) throws ServerCreationException {
		if (thriftHTTPServer == null) {
			thriftHTTPServer = ThriftHTTPServer.getInstance(config, getThriftProcessors());
		}
		return thriftHTTPServer;
	}

	/**
	 * Start all services according to configuration: Ticketing, REST, Thrift HTTP/TCP.
	 */
	public void startServices() {
		T config = readConfiguration();

		if (config == null) {
			logger.error("Configuration is null, could not start services.");
			return;
		}

		// Start ticketing, if enabled.
		if (config.getTicketServiceConfiguration().isEnabled()) {
			logger.info("Ticketing service enabled, starting.");
			TicketHandlerDaemon.start(config.getTicketServiceConfiguration());
		}

		DEFServerHolderConfiguration serverHolderConfiguration = config.getServerHolderConfiguration();

		// Start REST server, if enabled.
		if (serverHolderConfiguration.getRESTConfiguration().isEnabled()) {
			logger.debug("RESTServices enabled for {}, try to start.", serverClass);
			try {
				IServer server = getRESTServer(serverHolderConfiguration.getRESTConfiguration());

				ServiceRegistry.getInstance().registerService(RESTServer.class, server);
				Thread restServiceThread = new Thread(server);
				restServiceThread.start();
				logger.info("Started RESTServices successfully.");

			} catch (ServerCreationException e) {
				logger.error("Error while start RESTServices.", e);
			}
		}

		// Start Thrift HTTP server, if enabled.
		if (serverHolderConfiguration.getThriftHTTPConfiguration().isEnabled()) {
			logger.debug("Thrift HTTP enabled for {}, try to start.", serverClass);
			try {
				IServer server = getThriftHTTPServer(serverHolderConfiguration.getThriftHTTPConfiguration());

				ServiceRegistry.getInstance().registerService(ThriftHTTPServer.class, server);
				Thread thriftHTTPServiceThread = new Thread(server);
				thriftHTTPServiceThread.start();
				logger.info("Started Thrift HTTP Services successfully.");

			} catch (ServerCreationException e) {
				logger.error("Error while start Thrift HTTP Services.", e);
			}
		}

		// Start Thrift TCP server, if enabled.
		if (serverHolderConfiguration.getThriftTCPConfiguration().isEnabled()) {
			logger.debug("Thrift TCP enabled for {}, try to start.", serverClass);
			try {
				IServer server = getThriftTCPServer(serverHolderConfiguration.getThriftTCPConfiguration());

				ServiceRegistry.getInstance().registerService(ThriftTCPServer.class, server);
				Thread thriftTCPServiceThread = new Thread(server);
				thriftTCPServiceThread.start();
				logger.info("Started Thrift TCP successfully");

			} catch (ServerCreationException e) {
				logger.error("Error while start Thrift TCP Services.", e);
			}
		}
	}


	/**
	 * Returns a list of thrift processors, to create thrift service.
	 *
	 * @return List<ThriftProcessor> of registered thrift processors.
	 */
	protected abstract List<ThriftProcessor> getThriftProcessors();


	/**
	 * Returns a list of web resources to create a REST service.
	 *
	 * @return List<IResource> of registered web resources.
	 */
	protected abstract List<IResource> getWebResources();

	/**
	 * Returns Configuration.
	 * @return Configuration instance.
	 */
	public T getConfiguration() {
		return readConfiguration();
	}
}
