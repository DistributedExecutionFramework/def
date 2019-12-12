package at.enfilo.def.worker.impl;

import at.enfilo.def.datatype.DEFString;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.impl.ExecutorService;
import at.enfilo.def.node.impl.IStateChangeListener;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.routine.exec.SequenceStepsBuilder;
import at.enfilo.def.node.routine.exec.SequenceStepsExecutor;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.node.util.ResultUtil;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.RoutineType;
import at.enfilo.def.transfer.dto.TaskDTO;
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
class TaskExecutorService extends ExecutorService<TaskDTO> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(TaskExecutorService.class);
    private static WorkerConfiguration CONFIGURATION;

    private final TDeserializer deserializer;

    static {
        CONFIGURATION = Worker.getInstance().getConfiguration();
    }

    private final RoutineProcessBuilderFactory routineProcessBuilderFactory;

    public TaskExecutorService(
        QueuePriorityWrapper<TaskDTO> queuePriorityWrapper,
        RoutineProcessBuilderFactory routineProcessBuilderFactory,
        String storeRoutineId,
        IStateChangeListener stateChangeListener
    ) {
        super(
                queuePriorityWrapper,
                storeRoutineId,
                stateChangeListener,
                WorkerServiceController.DTO_TASK_CACHE_CONTEXT,
				TaskDTO.class
        );
        this.routineProcessBuilderFactory = routineProcessBuilderFactory;
        this.deserializer = new TDeserializer();
    }

    /**
     * Constructor for unit testing
     */
    protected TaskExecutorService(
            QueuePriorityWrapper<TaskDTO> queuePriorityWrapper,
            RoutineProcessBuilderFactory routineProcessBuilderFactory,
            String storeRoutineId,
            IStateChangeListener stateChangeListener,
            WorkerConfiguration configuration,
            DTOCache cache,
            TDeserializer deserializer
    ) {
        super(
                queuePriorityWrapper,
                storeRoutineId,
                stateChangeListener,
                cache
        );
        this.routineProcessBuilderFactory = routineProcessBuilderFactory;
        CONFIGURATION = configuration;
        this.deserializer = deserializer;
    }

    @Override
    protected void logInfo(String message) {
        LOGGER.info(message);
    }

    @Override
    protected void logInfo(TaskDTO task, String message) {
        LOGGER.info(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()), message);
    }

    @Override
    protected void logError(String message, Exception e) {
        LOGGER.error(message, e);
    }

    @Override
    protected void logError(TaskDTO task, String message, Exception e) {
        LOGGER.error(DEFLoggerFactory.createTaskContext(task.getProgramId(), task.getJobId(), task.getId()), message, e);
    }

    @Override
    protected String getElementId(TaskDTO task) {
        return task.getId();
    }

    @Override
    protected ExecutionState getElementState(TaskDTO task) {
        return task.getState();
    }

    @Override
    protected SequenceStepsExecutor buildSequenceStepsExecutor(TaskDTO task) {
        return new SequenceStepsBuilder(task.getId(), CONFIGURATION)
                .appendStep(task.getObjectiveRoutineId(), RoutineType.OBJECTIVE)
                .appendStep(task.getMapRoutineId(), RoutineType.MAP)
                .appendStep(getStoreRoutineId(), RoutineType.STORE)
                .build(task, this.routineProcessBuilderFactory);
    }

    @Override
    protected void prepareElementForExecution(TaskDTO task) {
        task.setState(ExecutionState.RUN);
        task.setStartTime(System.currentTimeMillis());
    }

    @Override
    protected List<Result> executeElement(TaskDTO task, SequenceStepsExecutor executor) throws Exception {
        executor.getCommunicator().addParameter("program", createResourceDTO(new DEFString(task.getProgramId())));
        if (CONFIGURATION.getParameterServerEndpoint() != null) {
            executor.getCommunicator().addParameter("parameterServerEndpoint", createResourceDTO(CONFIGURATION.getParameterServerEndpoint()));
        }

        List<Consumer<String>> processOutputConsumers = Collections.singletonList(task::addToMessages);
        return executor.run(processOutputConsumers, processOutputConsumers);
    }

    @Override
    protected void handleSuccessfulExecutionOfElement(TaskDTO task, List<Result> results) throws Exception {
        List<ResourceDTO> outParameters = extractResults(results);
        task.setOutParameters(outParameters);
        task.setState(ExecutionState.SUCCESS);
    }

    @Override
    protected void handleFailedExecutionOfElement(TaskDTO task, Exception e) {
        task.setState(ExecutionState.FAILED);
        task.addToMessages(e.getMessage());
    }

    protected List<ResourceDTO> extractResults(List<Result> results) {
        List<ResourceDTO> outParameters = new LinkedList<>();
        for (Result result: results) {
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
        return outParameters;
    }

    /**
     * Method for unit testing
     *
     * @param task
     */
	protected void runTask(TaskDTO task) {
        super.run(task);
    }
}
