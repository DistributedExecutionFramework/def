package at.enfilo.def.communication.thrift.tcp;

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
import at.enfilo.def.config.server.core.DEFServerEndpointConfiguration;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mase on 31.08.2016.
 */
public class ThriftTCPServer extends DEFServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ThriftTCPServer.class);
	private static final int MIN_THREADS = 2;
	private static final int MAX_THREADS = 4;

	private final TServer server;

	private ThriftTCPServer(
			DEFServerEndpointConfiguration serverConfig,
			List<ThriftProcessor> thriftProcessorList,
			boolean ticketService
	) throws ServerCreationException {

		super(Protocol.THRIFT_TCP, serverConfig, LOGGER);

		if (getUrlPattern().isEmpty()) {
			LOGGER.error("ThriftTCP Server cannot be created: URL pattern can not be empty.");
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

		// Registering processors.
		TMultiplexedProcessor multiplexedProcessor = new TMultiplexedProcessor();
		internalProcessorList.forEach(thriftProcessor -> {
				String pathSpec = forgePath(getUrlPattern(), thriftProcessor.getName());
				multiplexedProcessor.registerProcessor(
					pathSpec, thriftProcessor.getProcessor()
				);
				LOGGER.debug("Named processor \"{}\" was registered as {}.", thriftProcessor.getName(), pathSpec);
		});

		InetSocketAddress socketAddress = new InetSocketAddress(
				getServiceEndpoint().getHost(),
				getServiceEndpoint().getPort()
		);

		try {
			TServerTransport serverTransport = new TServerSocket(socketAddress);
			TThreadPoolServer.Args args = new TThreadPoolServer.Args(serverTransport);
			args.processor(multiplexedProcessor);
			args.minWorkerThreads(MIN_THREADS);
			args.minWorkerThreads(MAX_THREADS);
			server = new TThreadPoolServer(args);

//			TNonblockingServerTransport serverTransport = new TNonblockingServerSocket(socketAddress);
//			TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(serverTransport);
//			args.processor(multiplexedProcessor);
//			server = new TThreadedSelectorServer(args);


		} catch (TTransportException e) {
			throw new ServerCreationException(e);
		}
	}

	public static ThriftTCPServer getInstance(DEFServerEndpointConfiguration config, ThriftProcessor... thriftProcessors)
	throws ServerCreationException {
		return getInstance(config, Arrays.asList(thriftProcessors));
	}

	public static ThriftTCPServer getInstance(DEFServerEndpointConfiguration config, List<ThriftProcessor> thriftProcessorList)
	throws ServerCreationException {
		return getInstance(config, thriftProcessorList, true);
	}

	public static ThriftTCPServer getInstance(
			DEFServerEndpointConfiguration config,
			List<ThriftProcessor> thriftProcessorList,
			boolean ticketService
	) throws ServerCreationException {

		return new ThriftTCPServer(config, thriftProcessorList, ticketService);
	}

	@Override
	public boolean isRunning() {
		return server != null && server.isServing();
	}

	@Override
	protected void doStartServer()
	throws ServerStartupException {
		server.serve();
	}

	@Override
	protected void doStopServer()
	throws ServerShutdownException {
		server.stop();
	}
}
