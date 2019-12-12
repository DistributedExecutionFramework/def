package at.enfilo.def.worker.impl;

import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.routine.exec.SequenceStepsBuilder;
import at.enfilo.def.node.routine.exec.SequenceStepsExecutor;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.node.util.ResultUtil;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.RoutineType;
import at.enfilo.def.transfer.dto.TaskDTO;
import at.enfilo.def.worker.queue.QueuePriorityWrapper;
import at.enfilo.def.worker.server.Worker;
import at.enfilo.def.worker.util.WorkerConfiguration;
import org.apache.thrift.TDeserializer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Picks one by one task from queuePriorityWrapper and execute / run it.
 * A task is a sequence of routines: ObjectiveRoutine > MapRoutine > PartitionRoutine > StoreRoutine (for Worker).
 */
class TaskExecutorService extends Thread {

	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(TaskExecutorService.class);
	private static final WorkerConfiguration CONFIGURATION = Worker.getInstance().getConfiguration();

	private final QueuePriorityWrapper queuePriorityWrapper;
	private final ITaskStateChangeListener taskStateChangeListener;
	private final Object runningTaskLock;
	private final DTOCache<TaskDTO> taskCache;
	private final TDeserializer deserializer;
	private final RoutineProcessBuilderFactory routineProcessBuilderFactory;

	private boolean isActive;
	private SequenceStepsExecutor sequenceStepsExecutor;
	private String runningTask;
	private String storeRoutineId;

	/**
	 * Creates a TaskExecutorService for the given queuePriorityWrapper.
	 *
	 * @param queuePriorityWrapper - instance of queue priority wrapper.
	 */
	public TaskExecutorService(
		QueuePriorityWrapper queuePriorityWrapper,
		RoutineProcessBuilderFactory routineProcessBuilderFactory,
		String storeRoutineId,
		ITaskStateChangeListener taskStateChangeListener
	) {
		this.queuePriorityWrapper = queuePriorityWrapper;
		this.taskStateChangeListener = taskStateChangeListener;
		this.routineProcessBuilderFactory = routineProcessBuilderFactory;
		this.storeRoutineId = storeRoutineId;
		this.isActive = true;
		this.runningTaskLock = new Object();
		this.taskCache = DTOCache.getInstance(WorkerServiceController.DTO_TASK_CACHE_CONTEXT, TaskDTO.class);
		this.deserializer = new TDeserializer();
	}

	@Override
	public void run() {
		LOGGER.info("Start TaskExecutorService.");

		while (isActive) {
			try {
				// Fetch next task from queuePriorityWrapper and create a running sequence
				TaskDTO task = queuePriorityWrapper.enqueue();

				// Run (execute) a task
				runTask(task);

			} catch (Exception e) {
				LOGGER.error("Error while running TaskExecutorService.", e);
				isActive = false;
			}
		}

		LOGGER.info("TaskExecutorService terminated.");
	}

	/**
	 * Runs/executes a task. (split up to sequence steps)
	 *
	 * @param task - to execute
	 */
	void runTask(TaskDTO task) {
		LOGGER.debug(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()), "Prepare Task to run.");
		synchronized (runningTaskLock) {
			runningTask = task.getId();
		}

		// Change task to state RUN
		task.setState(ExecutionState.RUN);
		task.setStartTime(System.currentTimeMillis());
		taskCache.cache(task.getId(), task); // Update cache
		LOGGER.debug(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(),task.getId()), "Notify Task state changed from SCHEDULED to RUN.");
		taskStateChangeListener.notifyStateChanged(task.getId(), ExecutionState.SCHEDULED, ExecutionState.RUN);

		// Create Sequence Steps Executor for Task.
		sequenceStepsExecutor = new SequenceStepsBuilder(task.getId(), CONFIGURATION)
				.appendStep(task.getObjectiveRoutineId(), RoutineType.OBJECTIVE)
				.appendStep(task.getMapRoutineId(), RoutineType.MAP)
				.appendStep(storeRoutineId, RoutineType.STORE)
				.build(task, routineProcessBuilderFactory);

		// Run Task --> All Routines as  Processes
		try {
			LOGGER.info(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()), "Run Routine processes for Task.");
			List<Consumer<String>> processOutputConsumers = Collections.singletonList(task::addToMessages);
			// Run and wait for task results
			List<Result> results = sequenceStepsExecutor.run(processOutputConsumers, processOutputConsumers);
			sequenceStepsExecutor = null; // done, free references

			// Task successfully done
			LOGGER.debug(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()), "Task (all Routine processes) successfully finished.");
			List<ResourceDTO> outParameters = new LinkedList<>();
			for (Result result : results) {
				ResourceDTO resource = new ResourceDTO();
				resource.setId(UUID.randomUUID().toString());
				resource.setDataTypeId(ResultUtil.extractDataTypeId(result, deserializer));
				if (result.isSetUrl() && !result.getUrl().isEmpty()) {
					resource.setUrl(result.getUrl());
				} else {
					resource.setData(result.getData());
				}
				resource.setKey(result.getKey());
				outParameters.add(resource);
			}
			task.setOutParameters(outParameters);
			task.setState(ExecutionState.SUCCESS);

		} catch (Exception e) {
			// Task execution failed
			LOGGER.error(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()), "Failed to execution Task: {}.", e.getMessage(), e);
			task.setState(ExecutionState.FAILED);
			task.addToMessages(e.getMessage());
		}
		task.setFinishTime(System.currentTimeMillis());

		taskCache.cache(task.getId(), task); // Update cache

		synchronized (runningTaskLock) {
			runningTask = null;
		}

		LOGGER.info(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()), "Task finished with state \"{}\".", task.getState());

		// Notify Task is finished
		LOGGER.debug(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()), "Notify state changed from RUN to {}.", task.getState());
		taskStateChangeListener.notifyStateChanged(task.getId(), ExecutionState.RUN, task.getState());
		LOGGER.debug(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()), "Run finished.");
	}

	/**
	 * Cancel current running task.
	 */
	public void cancelRunningTask() {
		LOGGER.debug("Cancel running Task.");
		if (sequenceStepsExecutor != null) {
			sequenceStepsExecutor.cancel();
		}
		LOGGER.debug("Running task cancelled.");
	}

	/**
	 * Returns the id of current running task.
	 * @return
	 */
	public String getRunningTask() {
		synchronized (runningTaskLock) {
			LOGGER.debug("Get running Task.");
			return runningTask;
		}
	}

	public void shutdown() {
		LOGGER.debug("Shutdown command received.");

		if (sequenceStepsExecutor != null && sequenceStepsExecutor.isRunning()) {
			sequenceStepsExecutor.cancel();
		}

		isActive = false;
	}

	public void setStoreRoutine(String storeRoutineId) {
		LOGGER.debug("Set StoreRoutine with id {}.", storeRoutineId);
		this.storeRoutineId = storeRoutineId;
	}
}
