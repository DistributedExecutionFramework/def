package at.enfilo.def.communication.rest;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.exception.ServerShutdownException;
import at.enfilo.def.communication.exception.ServerStartupException;
import at.enfilo.def.communication.impl.meta.MetaServiceImpl;
import at.enfilo.def.communication.impl.ticket.TicketServiceImpl;
import at.enfilo.def.communication.misc.DEFServer;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;
import at.enfilo.def.security.filter.AuthenticationContainerRequestFilter;
import at.enfilo.def.security.filter.AuthorizationContainerRequestFilter;
import at.enfilo.def.security.filter.CORSFilter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mase on 31.08.2016.
 */
public class RESTServer extends DEFServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(RESTServer.class);

	private final Server server;

	private RESTServer(DEFServerEndpointConfiguration serverConfig, List<IResource> resourceList, boolean ticketService)
	throws ServerCreationException {
		super(Protocol.REST, serverConfig, LOGGER);

		if (getUrlPattern().isEmpty()) {
			throw new ServerCreationException("URL pattern can not be empty.");
		}

		List<IResource> internalResourceList = new LinkedList<>(resourceList);

		// [SERVER IMPLEMENTATION CONVENTION] Adding meta service resource to specified resource collection.
		internalResourceList.add(0, new MetaServiceImpl());

		if (ticketService) {
			// [SERVER IMPLEMENTATION CONVENTION] Adding ticket service resource to specified resource collection.
			internalResourceList.add(1, new TicketServiceImpl());
		}

		// Registering resource impl. instances.
		ResourceConfig resourceConfig = new ResourceConfig();
		internalResourceList.forEach(resource -> {
				String pathSpec = forgePath(getUrlPattern(), resource.getClass());
				resourceConfig.register(resource);
				LOGGER.info("REST resource \"{}\" was registered at {}.", resource.getClass().getName(), pathSpec);
		});

		// Registering JsonPOJO Mapper.
		resourceConfig.register(JacksonFeature.class);

		// Registering MIME / Multipart feature.
		resourceConfig.register(MultiPartFeature.class);

		// Registering security filters.
		resourceConfig.register(AuthenticationContainerRequestFilter.class);
		resourceConfig.register(AuthorizationContainerRequestFilter.class);
		resourceConfig.register(CORSFilter.class);

		// Assembling context handler.
		ServletContextHandler servletContextHandler = new ServletContextHandler();
		servletContextHandler.addServlet(
				new ServletHolder(new ServletContainer(resourceConfig)),
				getUrlPattern()
		);

		InetSocketAddress socketAddress = new InetSocketAddress(
				getServiceEndpoint().getHost(), getServiceEndpoint().getPort()
		);
		server = new Server(socketAddress);
		server.setHandler(servletContextHandler);
	}

	public static RESTServer getInstance(DEFServerEndpointConfiguration config, IResource... resources)
    throws ServerCreationException {
		return getInstance(config, Arrays.asList(resources));
	}

	public static RESTServer getInstance(DEFServerEndpointConfiguration config, List<IResource> resourceList)
    throws ServerCreationException {
		return getInstance(config, resourceList, true);
	}

	public static RESTServer getInstance(
			DEFServerEndpointConfiguration config,
			List<IResource> resourceList,
			boolean ticketService) throws ServerCreationException {

		return new RESTServer(config, resourceList, ticketService);
	}


	@Override
	public boolean isRunning() {
		return server != null && server.isStarted();
	}

	@Override
	protected void doStartServer()
    throws ServerStartupException {
		try {
			server.start();
		} catch (Exception e) {
			throw new ServerStartupException(e);
		}
	}

	@Override
	protected void doStopServer()
    throws ServerShutdownException {
		try {
			server.stop();
			server.destroy();
		} catch (Exception e) {
			throw new ServerShutdownException(e);
		}
	}
}
