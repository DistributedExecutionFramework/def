package at.enfilo.def.worker.impl;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.dto.cache.UnknownCacheObjectException;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.ContextSetBuilder;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.impl.ExecutorService;
import at.enfilo.def.node.impl.NodeServiceController;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.node.queue.Queue;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.transfer.UnknownTaskException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.NodeType;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;
import at.enfilo.def.worker.queue.TaskQueue;
import at.enfilo.def.worker.server.Worker;
import at.enfilo.def.worker.util.WorkerConfiguration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


/**
 * Holds all Worker Resources delegate requests.
 * This controller is served from {@link WorkerServiceImpl}
 */
public class WorkerServiceController extends NodeServiceController<TaskDTO> {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(WorkerServiceController.class);
	private static final String ELEMENT_NAME = "Task";

	public static final String DTO_TASK_CACHE_CONTEXT = "node-tasks";

	private final QueuePriorityWrapper<TaskDTO> queuePriorityWrapper;
	private final WorkerServiceClientFactory workerClientFactory;

	/**
	 * Private class to provide thread safe singleton
	 */
	private static class ThreadSafeLazySingletonWrapper {
		private static final WorkerServiceController INSTANCE = new WorkerServiceController();

		private ThreadSafeLazySingletonWrapper() {}
	}

	/**
	 * Singleton pattern.
	 * @return a instance of {@link WorkerServiceController}
	 */
	static WorkerServiceController getInstance() {
		return WorkerServiceController.ThreadSafeLazySingletonWrapper.INSTANCE;
	}


	/**
	 * Singleton, hide constructor
	 */
	private WorkerServiceController() {
		this(
			new QueuePriorityWrapper<>(Worker.getInstance().getConfiguration()),
			Collections.synchronizedSet(new HashSet<>()),
			new LinkedList<>(),
			new WorkerServiceClientFactory(),
			Worker.getInstance().getConfiguration(),
			new NodeObserverServiceClientFactory(),
			LOGGER
		);
	}

	/**
	 * Private constructor for unit tests
	 *
	 * @param queuePriorityWrapper - mapping between taskQueue and queue id
	 * @param finishedTasks - map of finished tasks
	 * @param observers - list of observers
	 * @param workerClientFactory - factory for creating worker service clients
	 */
	private WorkerServiceController(
			QueuePriorityWrapper<TaskDTO> queuePriorityWrapper,
			Set<String> finishedTasks,
			List<INodeObserverServiceClient> observers,
			WorkerServiceClientFactory workerClientFactory,
			WorkerConfiguration configuration,
			NodeObserverServiceClientFactory nodeObserverServiceClientFactory,
			IDEFLogger LOGGER
	) {
		super(
				NodeType.WORKER,
				observers,
				finishedTasks,
				configuration,
				nodeObserverServiceClientFactory,
				DTO_TASK_CACHE_CONTEXT,
				TaskDTO.class,
				LOGGER
		);
		this.queuePriorityWrapper = queuePriorityWrapper;
		this.workerClientFactory = workerClientFactory;

		try {
			RoutineProcessBuilderFactory routineProcessBuilderFactory = new RoutineProcessBuilderFactory(
					new LibraryServiceClientFactory().createClient(configuration.getLibraryEndpoint()),
					configuration
			);

			// Create TaskExecutorService for all threads.
			LOGGER.info("Start {} TaskExecutorServices.", configuration.getExecutionThreads());
			for (int i = 0; i < configuration.getExecutionThreads(); i++) {
				TaskExecutorService executorService = new TaskExecutorService(
						queuePriorityWrapper,
						routineProcessBuilderFactory,
						getStoreRoutineId(),
						this
				);

				executorService.setName("TaskExecutionThread " + i);
				executorService.setDaemon(true);
				executorService.start();
				executorServices.add(executorService);
			}
		} catch (ClientCreationException e) {
			LOGGER.error("Error while create LibraryServiceClient on startup.", e);
			throw new RuntimeException(e);
		}
	}

	public List<String> getQueueIds() {
		return queuePriorityWrapper.getAllQueues()
				.stream()
				.map(Queue::getQueueId)
				.collect(Collectors.toList());
	}

	@Override
	protected Queue createQueueInstance(String qId) {
		return new TaskQueue(qId);
	}

    @Override
    protected QueuePriorityWrapper getQueuePriorityWrapper() {
        return queuePriorityWrapper;
    }

	@Override
	protected List<? extends ExecutorService> getExecutorServices() {
		return executorServices;
	}

	/**
	 * Abort task: all running processes (sequence steps) will be killed
	 * If task is not running it will be removed from the queue.
	 * @param tId
	 */
	public void abortTask(String tId) {
		try {
			TaskDTO task = elementCache.fetch(tId);
			task.addToMessages("Aborted by user.");
			ExecutionState oldState = task.getState();

			abortElement(tId, task, oldState);
		} catch (IOException | UnknownCacheObjectException e) {
			LOGGER.error(getLogContext(tId), "Error while aborting task.");
		}
	}

	@Override
	protected void throwException(String eId, String message) throws Exception {
		throw new UnknownTaskException(message);
	}

	@Override
	protected Set<ITuple<ContextIndicator, ?>> getLogContext(TaskDTO element) {
		return new ContextSetBuilder()
				.add(ContextIndicator.PROGRAM_CONTEXT, element.getProgramId())
				.add(ContextIndicator.JOB_CONTEXT, element.getJobId())
				.add(ContextIndicator.TASK_CONTEXT, element.getId())
				.build();
	}

	@Override
	protected Set<ITuple<ContextIndicator, ?>> getLogContext(String elementId) {
		return new ContextSetBuilder()
				.add(ContextIndicator.TASK_CONTEXT, elementId)
				.build();
	}

	@Override
	protected void removeElementFromQueues(String eId) {
		queuePriorityWrapper.getAllQueues().forEach(taskQueue -> taskQueue.remove(eId));
	}

	@Override
	protected void setState(TaskDTO element, ExecutionState state) {
		element.setState(state);
	}

	@Override
	protected List<String> getElementIds(List<TaskDTO> elements) {
		return elements.stream().map(TaskDTO::getId).collect(Collectors.toList());
	}

	@Override
	protected List<? extends Queue> getQueues() {
		return queuePriorityWrapper.getAllQueues();
	}

	@Override
	protected Future<Void> queueElements(String qId, List<TaskDTO> elementsToQueue, ServiceEndpointDTO targetNodeEndpoint) throws ClientCreationException, ClientCommunicationException {
		IWorkerServiceClient targetNodeClient = workerClientFactory.createClient(targetNodeEndpoint);
		return targetNodeClient.queueTasks(qId, elementsToQueue);
	}

	@Override
	protected void notifyObservers(String nId, List<String> eIds) {
		LOGGER.debug("Notify all observers.");
		notifyAllObservers(observerClient -> observerClient.notifyTasksReceived(
				nId,
				eIds
		));
	}

	@Override
	protected void finishedExecutionOfElement(String eId) {
		// do nothing
	}

	@Override
	protected String getElementName() {
		return ELEMENT_NAME;
	}

	/**
	 * Helper function for unit testing
	 */
	protected DTOCache<TaskDTO> getTaskCache() {
		return elementCache;
	}
}
