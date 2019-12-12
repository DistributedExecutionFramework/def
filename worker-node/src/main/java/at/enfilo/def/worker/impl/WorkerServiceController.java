package at.enfilo.def.worker.impl;

import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.dto.TicketStatusDTO;
import at.enfilo.def.communication.exception.ClientCommunicationException;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.dto.cache.UnknownCacheObjectException;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.QueueNotExistsException;
import at.enfilo.def.node.impl.NodeServiceController;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.transfer.UnknownTaskException;
import at.enfilo.def.transfer.dto.*;
import at.enfilo.def.worker.api.IWorkerServiceClient;
import at.enfilo.def.worker.api.WorkerServiceClientFactory;
import at.enfilo.def.worker.queue.QueuePriorityWrapper;
import at.enfilo.def.worker.queue.TaskQueue;
import at.enfilo.def.worker.server.Worker;
import at.enfilo.def.worker.util.WorkerConfiguration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


/**
 * Holds all Worker Resources delegate requests.
 * This controller is served from {@link WorkerServiceImpl}
 */
public class WorkerServiceController extends NodeServiceController implements ITaskStateChangeListener {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(WorkerServiceController.class);

	public static final String DTO_TASK_CACHE_CONTEXT = "node-tasks";

	private final QueuePriorityWrapper queuePriorityWrapper;
	private final Object tasksLock;
	private final Set<String> finishedTasks;
	private final DTOCache<TaskDTO> taskCache;
	private final Set<String> runningTasks;
	private final List<TaskExecutorService> taskExecutorServices;
	private final WorkerServiceClientFactory workerClientFactory;

	private String storeRoutineId;

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
			new QueuePriorityWrapper(),
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
			QueuePriorityWrapper queuePriorityWrapper,
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
				configuration,
				nodeObserverServiceClientFactory,
				LOGGER
		);
		this.queuePriorityWrapper = queuePriorityWrapper;
		this.finishedTasks = finishedTasks;
		this.storeRoutineId = configuration.getStoreRoutineId();
		this.taskExecutorServices = new LinkedList<>();
		this.taskCache = DTOCache.getInstance(DTO_TASK_CACHE_CONTEXT, TaskDTO.class);
		this.runningTasks = new HashSet<>();
		this.workerClientFactory = workerClientFactory;
		this.tasksLock = new Object();

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
						storeRoutineId,
						this
				);

				executorService.setName("TaskExecutionThread " + i);
				executorService.setDaemon(true);
				executorService.start();
				taskExecutorServices.add(executorService);
			}
		} catch (ClientCreationException e) {
			LOGGER.error("Error while create LibraryServiceClient on startup.", e);
			throw new RuntimeException(e);
		}
	}

	List<String> getQueues() {
		return queuePriorityWrapper.getAllQueues()
				.stream()
				.map(TaskQueue::getQueueId)
				.collect(Collectors.toList());
	}

	QueueInfoDTO getQueueInfo(String qId) throws QueueNotExistsException {
		return queuePriorityWrapper.getQueue(qId).toQueueInfoDTO();
	}

	void createQueue(String qId) {
		LOGGER.debug("Create queue with id {}.", qId);
		// If queue does not exist already.
		if (!queuePriorityWrapper.containsQueue(qId)) {
			// Create queue, release queue.
			TaskQueue queue = new TaskQueue(qId);
			queue.release();

			// Register queue.
			queuePriorityWrapper.addQueue(queue);
		}
		LOGGER.debug("Queue {} created.", qId);
	}

	/**
	 * Move a list of task to another node and notifies observers dependent on isNotificationRequired flag.
	 *
	 * @param qId - id of queue (job).
	 * @param taskIds - tasks (Id list) to move.
	 * @param targetNodeEndpoint - service endpoint of target node.
	 * @param isNotificationRequired - true: notify registered observers.
	 */
	private void moveTasks(
			String qId,
			List<String> taskIds,
			ServiceEndpointDTO targetNodeEndpoint,
			boolean isNotificationRequired
	) throws ClientCreationException, QueueNotExistsException {

		LOGGER.debug("Move Tasks from Queue with id {}.", qId);
		TaskQueue queue = queuePriorityWrapper.getQueue(qId);

		IWorkerServiceClient targetNodeClient = workerClientFactory.createClient(targetNodeEndpoint);
		List<TaskDTO> tasksToMove = new LinkedList<>();
		for (String tId : taskIds) {
			tasksToMove.add(queue.remove(tId));
		}
		try {
			Future<Void> moveTasks = targetNodeClient.queueTasks(qId, tasksToMove);
			LOGGER.info(
					"Moving tasks to node \"{}\" done with state \"{}\".",
					targetNodeEndpoint,
					moveTasks.get()
			);
		} catch (ClientCommunicationException | InterruptedException | ExecutionException e) {
			LOGGER.error("Error while moving Tasks to target node.", e);
		}

		// Notify observers
		if (isNotificationRequired) {
			LOGGER.debug("Notify all observers.");
			notifyAllObservers(this::notifyObserverNodeInfo);
		}
	}


	public void pauseQueue(String qId) throws QueueNotExistsException {
		LOGGER.debug("Pause Queue with id {}.", qId);
		queuePriorityWrapper.getQueue(qId).pause();
	}

	public void deleteQueue(String qId) throws QueueNotExistsException {
		LOGGER.debug("Delete Queue with id {}.", qId);
		queuePriorityWrapper.delQueue(qId);
	}

	public void releaseQueue(String qId) throws QueueNotExistsException {
		LOGGER.debug("Release Queue with id {}.", qId);
		queuePriorityWrapper.getQueue(qId).release();
	}

	public List<String> getQueuedTasks(String qId) throws QueueNotExistsException {
		return queuePriorityWrapper.getQueue(qId).getQueuedTasks();
	}

	public void queueTasks(String qId, TaskDTO... tasks) throws QueueNotExistsException, IOException, UnknownCacheObjectException {
		queueTasks(qId, Arrays.asList(tasks));
	}

	public void queueTasks(String qId, List<TaskDTO> taskList) throws QueueNotExistsException, IOException, UnknownCacheObjectException {
		LOGGER.debug("Try to queue a list of tasks to Queue with id {}.", qId);

		TaskQueue taskQueue = queuePriorityWrapper.getQueue(qId);
		try {
			// Add tasks to queue.
			for (TaskDTO task : taskList) {
				task.setState(ExecutionState.SCHEDULED);
				taskQueue.queue(task);
				LOGGER.info(getLogContext(task), "Queue Task successful.");
			}

			// Notify all observers.
			List<String> tIds = taskList.stream().map(TaskDTO::getId).collect(Collectors.toList());
			LOGGER.debug("Notify all observers.");
			notifyAllObservers(observerClient -> observerClient.notifyTasksReceived(
					getNodeConfiguration().getId(),
					tIds
			));

		} catch (InterruptedException e) {
			LOGGER.error("Error while queueing task.", e);
			Thread.currentThread().interrupt();
		}
	}

	public void moveTasks(
			String qId,
			List<String> taskIds,
			ServiceEndpointDTO targetNodeEndpoint
	) throws ClientCreationException, QueueNotExistsException {
		moveTasks(qId, taskIds, targetNodeEndpoint, true);
	}

	public void moveAllTasks(ServiceEndpointDTO targetNodeEndpoint)
			throws ClientCreationException, QueueNotExistsException {
		LOGGER.debug("Try to move all tasks to target node: \"{}\".", targetNodeEndpoint);

		for (TaskQueue taskQueue : queuePriorityWrapper.getAllQueues()) {
			moveTasks(taskQueue.getQueueId(), taskQueue.getQueuedTasks(), targetNodeEndpoint, false);
		}


		// Notify observers.
		LOGGER.debug("Notify all observers.");
		notifyAllObservers(this::notifyObserverNodeInfo);
	}

	public TaskDTO fetchFinishedTask(String tId) throws UnknownTaskException, IOException {
		synchronized (tasksLock) {
			LOGGER.debug(DEFLoggerFactory.createTaskContext(tId), "Fetch finished Task");
			if (!finishedTasks.contains(tId)) {
				String msg = "Task is not known as finished task by this node.";
				LOGGER.error(DEFLoggerFactory.createTaskContext(tId), msg);
				throw new UnknownTaskException(msg);
			}
		}

		try {
			TaskDTO task = taskCache.fetch(tId);
			synchronized (tasksLock) {
				finishedTasks.remove(tId);
			}
			taskCache.remove(tId);
			return task;
		} catch (IOException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(tId), "Error while fetch finished task from TaskCache.", e);
			throw e;
		} catch (UnknownCacheObjectException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(tId), "Error while fetch finished task from TaskCache.", e);
			throw new UnknownTaskException(e.getMessage());
		}
	}

	/**
	 * Abort task: all running processes (sequence steps) will be killed
	 * If task is not running it will be removed from the queue.
	 * @param tId
	 */
	public void abortTask(String tId) {
		LOGGER.debug(DEFLoggerFactory.createTaskContext(tId), "Abort Task");
		try {
			TaskDTO task = taskCache.fetch(tId);
			task.addToMessages("Aborted by user.");
			ExecutionState oldState = task.getState();
			if (runningTasks.contains(tId)) {
				LOGGER.info(DEFLoggerFactory.createTaskContext(tId), "Try to abort running Task.");
				abortRunningTask(tId);
				LOGGER.info(DEFLoggerFactory.createTaskContext(tId), "Task aborted.");
			} else {
				// Remove task from queue
				LOGGER.info(DEFLoggerFactory.createTaskContext(tId), "Remove Task from Queue.");
				queuePriorityWrapper.getAllQueues().forEach(taskQueue -> taskQueue.remove(tId));
			}
			task.setState(ExecutionState.FAILED);
			taskCache.cache(task.getId(), task);
			notifyStateChanged(tId, oldState, ExecutionState.FAILED);

		} catch (IOException | UnknownCacheObjectException e) {
			LOGGER.error(DEFLoggerFactory.createTaskContext(tId), "Error while abort Task.", e);
		}
	}

	@Override
	public void notifyStateChanged(String tId, ExecutionState oldState, ExecutionState newState) {
		synchronized (tasksLock) {
			LOGGER.debug(DEFLoggerFactory.createTaskContext(tId), "Notify state of Task changed from {} to {}.", oldState, newState);
			switch (oldState) {
				case RUN:
					runningTasks.remove(tId);
					break;
				case SCHEDULED:
				case SUCCESS:
				case FAILED:
				default:
					break;
			}
			switch (newState) {
				case RUN:
					runningTasks.add(tId);
					break;
				case SUCCESS:
				case FAILED:
					finishedTasks.add(tId);
					break;
				case SCHEDULED:
				default:
					break;
			}
		}

		// Notify observers
		List<String> taskList = Collections.singletonList(tId);
		LOGGER.debug("Notify all observers.");
		notifyAllObservers(observerClient -> observerClient.notifyTasksNewState(
				getNodeConfiguration().getId(),
				taskList,
				newState
		));
	}

	private void verifyTaskState(TaskDTO task, ExecutionState expectedState) {
		if (task.getState() != expectedState) {
			LOGGER.warn(
					DEFLoggerFactory.createTaskContext(task.getId()),
					"State is {} instead of {}. Change to {}.",
					task.getState(),
					expectedState,
					expectedState
			);
			task.setState(expectedState);
		}
	}

	String getStoreRoutineId() {
		return storeRoutineId;
	}

	void setStoreRoutineId(String storeRoutineId) {
		this.storeRoutineId = storeRoutineId;
		taskExecutorServices.forEach(tes -> tes.setStoreRoutine(storeRoutineId));
	}

	/**
	 * Abort a running task.
	 * @param tId
	 */
	private void abortRunningTask(String tId) {
		LOGGER.debug(DEFLoggerFactory.createTaskContext(tId), "Abort running Task.");
		for (TaskExecutorService executor : taskExecutorServices) {
			if (executor.getRunningTask() != null && executor.getRunningTask().equalsIgnoreCase(tId)) {
				executor.cancelRunningTask();
				break;
			}
		}
		LOGGER.info(DEFLoggerFactory.createTaskContext(tId), "Task aborted");
	}

	@Override
	protected Map<String, String> getNodeInfoParameters() {
		Map<String, String> params = new HashMap<>();
		params.put("numberOfQueues", Integer.toString(getNumberOfQueues()));
		params.put("numberOfQueuedTasks", Integer.toString(getNumberOfQueuedTasks()));
		params.put("numberOfRunningTasks", Integer.toString(getNumberOfRunningTasks()));
		synchronized (tasksLock) {
			params.put("runningTasks", runningTasks.stream().collect(Collectors.joining(" ")));
		}
		params.put("storeRoutineId", storeRoutineId);
		return params;
	}

	private int getNumberOfQueues() {
		return queuePriorityWrapper.getNumberOfQueues();
	}

	public int getNumberOfQueuedTasks() {
		return queuePriorityWrapper.getNumberOfQueuedTasks();
	}

	public int getNumberOfRunningTasks() {
		LOGGER.debug("Fetch number of running Tasks.");
		synchronized (tasksLock) {
			return runningTasks.size();
		}
	}

}
