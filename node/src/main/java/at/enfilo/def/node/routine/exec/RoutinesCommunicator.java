package at.enfilo.def.node.routine.exec;


import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.common.util.DaemonThreadFactory;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.dto.cache.UnknownCacheObjectException;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.impl.NodeServiceController;
import at.enfilo.def.routine.api.*;
import at.enfilo.def.routine.util.DataReader;
import at.enfilo.def.routine.util.DataWriter;
import at.enfilo.def.routine.util.Pipe;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.util.ResourceUtil;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Responsible for handling the communication from and to the routines.
 *
 * For every routine an own RoutineCommunicationHandler will be started.
 */
public class RoutinesCommunicator implements IPipeWriter, Runnable {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(RoutinesCommunicator.class);
	private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(r -> DaemonThreadFactory.newDaemonThread(r, "RoutineCommunicationHandler"));


	private class RoutineCommunicationHandler implements IPipeReader, Runnable {
		private final DataReader ctrlRead;
		private final DataWriter ctrlWrite;
		private final DataWriter out;
		private final Object waitParameterLock;
		private final CountDownLatch waitLock;
		private final String name;
		private boolean running;
		private IDEFLogger routineLogger;

		public RoutineCommunicationHandler(DataReader ctrlRead, DataWriter ctrlWrite, DataWriter out, String name) {
			this.waitParameterLock = new Object();
			this.ctrlRead = ctrlRead;
			this.ctrlWrite = ctrlWrite;
			this.out = out;
			this.name = name;
			this.waitLock = new CountDownLatch(1);
			this.running = false;
			this.routineLogger = DEFLoggerFactory.getLogger(name);
		}

		@Override
		public void run() {
			running = true;
			while (running) {
				// Loop to receive Orders from Routine
				receiveAndProcessOrder();
			}
			LOGGER.debug("RoutineCommunicationHandler for Routine '{}' done, close Ctrl-Pipe.", name);
			waitLock.countDown();
		}

		public void await() throws InterruptedException {
			waitLock.await();
		}

		/**
		 * Receives orders from ctrlRead pipe and process the order.
		 */
		private void receiveAndProcessOrder() {
			try {

				LOGGER.debug(logContext, "Waiting for Order");
				Order o = ctrlRead.read(new Order());
				LOGGER.debug(logContext, "Received Order: {}", o);

				if (o.getCommand() != null) {
					switch (o.getCommand()) {
						case GET_PARAMETER:
							handleGetParameter(o.getValue(), out);
							break;

						case GET_PARAMETER_KEY:
							handleGetParameterKey(o.getValue(), out);
							break;

						case SEND_RESULT:
							handleSendResults(o.getValue(), ctrlRead);
							break;

						case CANCEL:
						case ROUTINE_DONE:
							running = false;
							LOGGER.debug(logContext, "Received {}, shutting down RoutineCommunicationHandler", o.getCommand());
							break;

						case LOG_DEBUG:
							routineLogger.debug(logContext, o.getValue());
							break;
						case LOG_INFO:
							routineLogger.info(logContext, o.getValue());
							break;
						case LOG_ERROR:
							routineLogger.error(logContext, o.getValue());
							break;

						default:
							LOGGER.warn(logContext, "Unknown Command {} received", o.getCommand());
					}
				} else {
					LOGGER.warn(logContext, "Received a wrong/defect/unknown Order: {}", o);
					ctrlRead.reset();
				}

			} catch (InterruptedException e) {
				running = false;
				LOGGER.info(logContext, "Received interrupt/shutdown from Worker, shutting down.");
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				LOGGER.error(logContext, "Error while receiving/processing order, reset and continue.", e);
				ctrlRead.reset();
			}
		}

		/**
		 * Handle Order SEND_RESULT_INFO: receive ResultInfo structs from StoreRoutine.
		 *
		 * @param value - nr of ResultInfo objects
		 * @param ctrl - DataReader
		 * @throws IOException
		 * @throws TException
		 */
		private void handleSendResults(String value, DataReader ctrl) throws TException {
			Integer nrOfResultInfos = Integer.parseInt(value);
			synchronized (resultInfoLock) {
				for (int i = 0; i < nrOfResultInfos; i++) {
					Result result = new Result();
					ctrl.read(result);
					results.add(result);
				}
			}
			LOGGER.debug(logContext, "{} ResultInfo objects received", nrOfResultInfos);
		}

		/**
		 * Handle Order GET_PARAMETER: Send data of requested InParameter to SimpleRoutine.
		 *
		 * @param name - name of parameter
		 * @param out - DataWriter
		 * @throws IOException
		 * @throws TTransportException
		 */
		private void handleGetParameter(String name, DataWriter out) throws IOException, TTransportException, InterruptedException {
			LOGGER.debug(logContext, "Send data of Parameter {} to ObjectiveRoutine", name);
			ResourceDTO parameter = getParameterByName(name, true);
			byte[] data = new byte[]{};
			if (parameter != null) {
				if (ResourceUtil.isSharedResource(parameter)) {
					try {
						data = sharedResourceCache.fetch(parameter.getId()).data.array();
					} catch (Exception e) {
						LOGGER.error(logContext, "Error while replace Parameter {} with SharedResource .", name, e);
					}
				} else {
					data = parameter.data.array();
				}
			} else {
				LOGGER.warn("Parameter with name {} has no data assigned - send an empty byte[] to ObjectiveRoutine.", name);
			}
			out.store(data);
		}

		private void handleGetParameterKey(String name, DataWriter out) throws TException, IOException, InterruptedException, UnknownCacheObjectException {
			LOGGER.debug(logContext, "Send key of Parameter {} to ObjectiveRoutine", name);
			ResourceDTO parameter = getParameterByName(name, false);
			if (parameter != null) {
				out.store(parameter.getKey());
			} else {
				out.store(new byte[]{});
			}
		}

		private ResourceDTO getParameterByName(String name, boolean removeFromMap) throws InterruptedException, IOException {
			do {
				if (parameters.containsKey(name)) {
					if (removeFromMap) {
						return parameters.remove(name);
					}
					return parameters.get(name);
				} else {
					if (waitForParameter) {
						LOGGER.debug("Wait for parameter with name {}.", name);
						synchronized (waitParameterLock) {
							waitParameterLock.wait();
						}
					} else {
						LOGGER.error(logContext, "Parameter with name {} not provided", name);
						throw new IOException(String.format("Parameter with name %s not found", name));
					}
				}
			} while (true);
		}

		public void notifyNewParameter() {
			synchronized (waitParameterLock) {
				waitParameterLock.notify();
			}
		}

		public void shutdown() {
			if (running) {
				try {
					ctrlWrite.store(new Order(Command.CANCEL, ""));
				} catch (TException e) {
					LOGGER.error("Error while send CANCEL to handler.", e);
				}
			}
		}
	}

	private final Map<String, ResourceDTO> parameters;
	private boolean waitForParameter;
	private final List<Result> results;
	private final List<Pipe> ctrlPipes;
	private final Pipe outPipe;
	private final Object resultInfoLock = new Object();
	private final List<RoutineCommunicationHandler> handlers;
	private final Set<ITuple<ContextIndicator, ?>> logContext;
	private final DTOCache<ResourceDTO> sharedResourceCache;
	private boolean ready;

	/**
	 * Constructor to create SimpleRoutineCommunicator.
	 */
	public RoutinesCommunicator(
		Map<String, ResourceDTO> initialParameters,
		boolean waitForParameter,
		Pipe outPipe,
		List<Pipe> ctrlPipes,
		Set<ITuple<ContextIndicator, ?>> logContext
	) {
		if (initialParameters == null) {
			this.parameters = new ConcurrentHashMap<>();
		} else {
			this.parameters = new ConcurrentHashMap<>(initialParameters);
		}
		this.waitForParameter = waitForParameter;
		this.results = new LinkedList<>();
		this.outPipe = outPipe;
		this.outPipe.setWriter(this);
		this.ctrlPipes = ctrlPipes;
		this.handlers = new LinkedList<>();
		this.logContext = logContext;
		this.ready = false;
		this.sharedResourceCache = DTOCache.getInstance(NodeServiceController.DTO_RESOURCE_CACHE_CONTEXT, ResourceDTO.class);
	}

	@Override
	public void run() {

		LOGGER.debug(logContext, "Starting {}", this.getClass().getName());

		// Replace sharedResource proxies
		/*
		for (Map.Entry<String, ResourceDTO> entry : parameters.entrySet()) {
			ResourceDTO resource = replaceSharedResourceProxy(entry.getKey(), entry.getValue());
			entry.setValue(resource);
		}
		 */

		LOGGER.debug(logContext,"Open out pipe {}", outPipe);
		try (DataWriter out = new DataWriter(outPipe.getOutputStream())) {

			// Start RoutineCommunicationHandlers for every routine/ctrlRead-pipe
			int i = 0;
			for (Pipe ctrlPipe : ctrlPipes) {
				LOGGER.debug(logContext,"Open ctrlRead pipe {}", ctrlPipe);
				DataReader ctrlRead = new DataReader(ctrlPipe.getInputStream());
				DataWriter ctrlWrite = new DataWriter(ctrlPipe.getOutputStream());
				RoutineCommunicationHandler handler = new RoutineCommunicationHandler(ctrlRead, ctrlWrite, out, String.format("Routine %d", i));
				ctrlPipe.setReader(handler);
				THREAD_POOL.submit(handler);
				handlers.add(handler);
				i++;
			}

			ready = true;

			// Wait for all RoutineCommunicationHandlers
			for (RoutineCommunicationHandler handler : handlers) {
				handler.await();
				handler.ctrlRead.close();
				handler.ctrlWrite.close();
			}

			LOGGER.debug(logContext, "All RoutineCommunicationHandlers terminated successfully, therefore all routines done.");
			ready = false;

		} catch (IOException e) {
			LOGGER.error(logContext, "Error while open pipes", e);
		} catch (InterruptedException e) {
			LOGGER.info(logContext, "Interrupted RoutineCommunicationHandlers.");
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Returns all received ResultInfos.
	 *
	 * @return List of ResultInfo objects
	 */
	public List<Result> getResults() {
		return results;
	}

	/**
	 * Returns true if ResultInfos are available.
	 *
	 * @return false if List<ResultInfo> <= 0
	 */
	public boolean hasResultInfos() {
		synchronized (resultInfoLock) {
			return !results.isEmpty();
		}
	}

	/**
	 * Shutdown RoutineCommunicator.
	 */
	public void shutdown() {
		LOGGER.debug(logContext, "Shutdown called from Worker.");
		handlers.forEach(RoutineCommunicationHandler::shutdown);
	}

	public boolean isReady() {
		return ready;
	}

	public boolean isDown() {
		return !ready;
	}

	private void notifyNewParameter() {
		handlers.forEach(RoutineCommunicationHandler::notifyNewParameter);
	}

	public void addParameter(String key, ResourceDTO value) {
//		value = replaceSharedResourceProxy(key, value);
		parameters.put(key, value);
		notifyNewParameter();
	}

	public ResourceDTO getParameter(String key) {
		return parameters.get(key);
	}

	public void setWaitForParameter(boolean waitForParameter) {
		this.waitForParameter = waitForParameter;
	}

	/*
	private ResourceDTO replaceSharedResourceProxy(String key, ResourceDTO resource) {
		if (ResourceUtil.isSharedResource(resource)) {
			try {
				return sharedResourceCache.fetch(resource.getId()).get();
			} catch (UnknownCacheObjectException | IOException e) {
				LOGGER.error(logContext, "Error while replace SharedResource proxy for Resource '{}'.", key, e);
			}
		}
		return resource;
	}
	 */
}
