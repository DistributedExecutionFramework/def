package at.enfilo.def.communication.thrift.http;

import at.enfilo.def.communication.api.common.service.IResource;
import at.enfilo.def.communication.api.meta.thrift.MetaService;
import at.enfilo.def.communication.api.ticket.thrift.TicketService;
import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.exception.ServerCreationException;
import at.enfilo.def.communication.exception.ServerShutdownException;
import at.enfilo.def.communication.exception.ServerStartupException;
import at.enfilo.def.communication.impl.meta.MetaServiceImpl;
import at.enfilo.def.communication.impl.ticket.TicketServiceImpl;
import at.enfilo.def.communication.misc.DEFServer;
import at.enfilo.def.communication.thrift.ThriftProcessor;
import at.enfilo.def.communication.thrift.http.adapters.SecuredThriftServlet;
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mase on 14.09.2016.
 */
public class ThriftHTTPServer extends DEFServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThriftHTTPServer.class);

	private final Server server;

	private ThriftHTTPServer(
			DEFServerEndpointConfiguration serverConfig,
			List<ThriftProcessor> thriftProcessorList,
			boolean ticketService
	) throws ServerCreationException {

		super(Protocol.THRIFT_HTTP, serverConfig, LOGGER);

		if (getUrlPattern().isEmpty()) {
			throw new ServerCreationException("URL pattern can not be empty.");
		}

		List<ThriftProcessor> internalProcessorList = new LinkedList<>(thriftProcessorList);

		// [SERVER IMPLEMENTATION CONVENTION] Adding meta service processor to specified processor collection.
		internalProcessorList.add(0, new ThriftProcessor<>(
				MetaService.class.getName(),
				new MetaServiceImpl(),
				MetaService.Processor<MetaService.Iface>::new
		));

		if (ticketService) {
			// [SERVER IMPLEMENTATION CONVENTION] Adding ticket service processor to specified processor collection.
			internalProcessorList.add(1, new ThriftProcessor<>(
					TicketService.class.getName(),
					new TicketServiceImpl(),
					TicketService.Processor<TicketService.Iface>::new
			));
		}

		// Registering service impl. instances.
		ServletContextHandler servletContextHandler = new ServletContextHandler();
		internalProcessorList.forEach(thriftProcessor -> {
				String pathSpec = forgePath(getUrlPattern(), thriftProcessor.getName());
				// Security checks are integrated into SecuredThriftServlet.
				ServletHolder servletHolder = new ServletHolder(
						thriftProcessor.getName(),
						new SecuredThriftServlet(
								(ThriftProcessor<? extends IResource>) thriftProcessor,
								new TBinaryProtocol.Factory()
						)
				);
				servletContextHandler.addServlet(servletHolder, pathSpec);
				LOGGER.info("Named processor \"{}\" was registered as {}.", thriftProcessor.getName(), pathSpec);
		});

		InetSocketAddress socketAddress = new InetSocketAddress(
				getServiceEndpoint().getHost(),
				getServiceEndpoint().getPort()
		);

		server = new Server(socketAddress);
		server.setHandler(servletContextHandler);
	}

	public static ThriftHTTPServer getInstance(DEFServerEndpointConfiguration config, ThriftProcessor... thriftProcessors)
	throws ServerCreationException {
		return getInstance(config, Arrays.asList(thriftProcessors));
	}

	public static ThriftHTTPServer getInstance(DEFServerEndpointConfiguration config, List<ThriftProcessor> thriftProcessorList)
	throws ServerCreationException {
		return getInstance(config, thriftProcessorList, true);
	}

	public static ThriftHTTPServer getInstance(
			DEFServerEndpointConfiguration config,
			List<ThriftProcessor> thriftProcessorList,
			boolean ticketService
	) throws ServerCreationException {
		return new ThriftHTTPServer(config, thriftProcessorList, ticketService);
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
