package at.enfilo.def.reducer.impl;

import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.exception.QueueNotExistsException;
import at.enfilo.def.node.impl.ExecutorService;
import at.enfilo.def.node.impl.IStateChangeListener;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.queue.ResourceQueue;
import at.enfilo.def.node.routine.exec.SequenceStepsBuilder;
import at.enfilo.def.node.routine.exec.SequenceStepsExecutor;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.node.util.ResultUtil;
import at.enfilo.def.reducer.server.Reducer;
import at.enfilo.def.reducer.util.ReducerConfiguration;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ReduceJobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
import at.enfilo.def.transfer.dto.RoutineType;
import org.apache.thrift.TDeserializer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class ReduceJobExecutorService extends ExecutorService<ReduceJobDTO> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ReduceJobExecutorService.class);
    private static ReducerConfiguration CONFIGURATION;

    private final Map<String, ResourceQueue> resourceQueues;
    private final TDeserializer deserializer;

    static {
        CONFIGURATION = Reducer.getInstance().getConfiguration();
    }

    private final RoutineProcessBuilderFactory routineProcessBuilderFactory;

    public ReduceJobExecutorService(
            QueuePriorityWrapper<ReduceJobDTO> queuePriorityWrapper,
            Map<String, ResourceQueue> resourceQueues,
            RoutineProcessBuilderFactory routineProcessBuilderFactory,
            String storeRoutineId,
            IStateChangeListener stateChangeListener
    ) {
        super(
                queuePriorityWrapper,
                storeRoutineId,
                stateChangeListener,
                ReducerServiceController.DTO_JOB_CACHE_CONTEXT,
                ReduceJobDTO.class
        );
        this.resourceQueues = resourceQueues;
        this.routineProcessBuilderFactory = routineProcessBuilderFactory;
        this.deserializer = new TDeserializer();
    }

    /**
     * Constructor for unit testing
     */
    protected ReduceJobExecutorService(
            QueuePriorityWrapper<ReduceJobDTO> queuePriorityWrapper,
            Map<String, ResourceQueue> resourceQueues,
            RoutineProcessBuilderFactory routineProcessBuilderFactory,
            String storeRoutineId,
            IStateChangeListener stateChangeListener,
            ReducerConfiguration configuration,
            DTOCache cache,
            TDeserializer deserializer
    ) {
        super(
                queuePriorityWrapper,
                storeRoutineId,
                stateChangeListener,
                cache
        );
        this.resourceQueues = resourceQueues;
        this.routineProcessBuilderFactory = routineProcessBuilderFactory;
        CONFIGURATION = configuration;
        this.deserializer = deserializer;
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

    @Override
    protected void logInfo(String message) {
        LOGGER.info(message);
    }

    @Override
    protected void logInfo(ReduceJobDTO reduceJob, String message) {
        LOGGER.info(DEFLoggerFactory.createJobContext(reduceJob.getJob().getProgramId(), reduceJob.getJobId()), message);
    }

    @Override
    protected void logError(String message, Exception e) {
        LOGGER.error(message, e);
    }

    @Override
    protected void logError(ReduceJobDTO reduceJob, String message, Exception e) {
        LOGGER.error(DEFLoggerFactory.createJobContext(reduceJob.getJob().getProgramId(), reduceJob.getJobId()), message, e);
    }

    @Override
    protected String getElementId(ReduceJobDTO reduceJob) {
        return reduceJob.getJobId();
    }

    @Override
    protected ExecutionState getElementState(ReduceJobDTO reduceJob) {
        return reduceJob.getState();
    }

    @Override
    protected SequenceStepsExecutor buildSequenceStepsExecutor(ReduceJobDTO reduceJob) throws QueueNotExistsException {
        SequenceStepsExecutor executor = new SequenceStepsBuilder(reduceJob.getJobId(), CONFIGURATION)
                .appendStep(reduceJob.getJob().getReduceRoutineId(), RoutineType.REDUCE)
                .appendStep(getStoreRoutineId(), RoutineType.STORE)
                .build(reduceJob, this.routineProcessBuilderFactory);
        if (!resourceQueues.containsKey(reduceJob.getJobId())) {
            LOGGER.error(DEFLoggerFactory.createJobContext(reduceJob.getJobId()), "ResourceQueue not exists.");
            throw new QueueNotExistsException(String.format("ResourceQueue with id %s not known.", reduceJob.getJobId()));
        }
        resourceQueues.get(reduceJob.getJobId()).registerObserver(executor.getCommunicator());
        return executor;
    }

    @Override
    protected void prepareElementForExecution(ReduceJobDTO reduceJob) {
        reduceJob.setState(ExecutionState.RUN);
        reduceJob.setStartTime(System.currentTimeMillis());
    }

    @Override
    protected List<Result> executeElement(ReduceJobDTO reduceJob, SequenceStepsExecutor executor) throws Exception {
        List<Consumer<String>> processOutputConsumers = Collections.singletonList(reduceJob::addToMessages);
        return executor.run(processOutputConsumers, processOutputConsumers);
    }

    @Override
    protected void handleSuccessfulExecutionOfElement(ReduceJobDTO reduceJob, List<Result> results) throws Exception {
        List<ResourceDTO> reducedResults = extractResults(results);
        reduceJob.getJob().setReducedResults(reducedResults);
        reduceJob.setState(ExecutionState.SUCCESS);
    }

    @Override
    protected void handleFailedExecutionOfElement(ReduceJobDTO reduceJob, Exception e) {
        reduceJob.setState(ExecutionState.FAILED);
        reduceJob.addToMessages(e.getMessage());
    }
}
