package at.enfilo.def.reducer.impl;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.dto.cache.UnknownCacheObjectException;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.ContextSetBuilder;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.exception.QueueNotExistsException;
import at.enfilo.def.node.impl.ExecutorService;
import at.enfilo.def.node.impl.NodeServiceController;
import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.node.queue.Queue;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.queue.ResourceQueue;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.reducer.queue.ReduceJobQueue;
import at.enfilo.def.reducer.server.Reducer;
import at.enfilo.def.reducer.util.ReducerConfiguration;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.dto.*;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ReducerServiceController extends NodeServiceController<ReduceJobDTO> {

    private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(ReducerServiceController.class);
    private static final String ELEMENT_NAME = "ReduceJob";

    public static final String DTO_JOB_CACHE_CONTEXT = "reducer-jobs";
    public static final String DTO_RESOURCE_CACHE_CONTEXT = "reducejob-resources";

    protected static final String KEY_REDUCE = "REDUCE";

    private final QueuePriorityWrapper<ReduceJobDTO> reduceJobQueuePriorityWrapper;
    private final Map<String, ResourceQueue> resourceQueues;
    private final Map<String, CountDownLatch> activeReduceJobs;

    protected final List<ReduceJobExecutorService> reduceJobExecutorServices;

    private String storeRoutineId;


    /**
     * Private class to provide thread safe singleton
     */
    private static class ThreadSafeLazySingletonWrapper {
        private static final ReducerServiceController INSTANCE = new ReducerServiceController();

        private ThreadSafeLazySingletonWrapper() {}
    }

    /**
     * Singleton pattern
     * @return an instance of {@link ReducerServiceController}
     */
    static ReducerServiceController getInstance() {
        return ThreadSafeLazySingletonWrapper.INSTANCE;
    }

    /**
     * Singleton, hide constructor
     */
    private ReducerServiceController() {
        this(
            new QueuePriorityWrapper<>(Reducer.getInstance().getConfiguration()),
            new HashMap<>(),
            Collections.synchronizedSet(new HashSet<>()),
            new LinkedList<>(),
            Reducer.getInstance().getConfiguration(),
            new NodeObserverServiceClientFactory()
        );
    }

    /**
     * Private constructor for unit tests
     */
    private ReducerServiceController(
            QueuePriorityWrapper<ReduceJobDTO> reduceJobQueuePriorityWrapper,
            Map<String, ResourceQueue> resourceQueues,
            Set<String> finishedJobs,
            List<INodeObserverServiceClient> observers,
            ReducerConfiguration configuration,
            NodeObserverServiceClientFactory nodeObserverServiceClientFactory
    ) {
        super(
                NodeType.REDUCER,
                observers,
                finishedJobs,
                configuration,
                nodeObserverServiceClientFactory,
                DTO_JOB_CACHE_CONTEXT,
                ReduceJobDTO.class,
                LOGGER
        );
        this.reduceJobQueuePriorityWrapper = reduceJobQueuePriorityWrapper;
        this.resourceQueues = resourceQueues;
        this.storeRoutineId = configuration.getStoreRoutineId();
        this.reduceJobExecutorServices = new LinkedList<>();
        this.activeReduceJobs = new HashMap<>();

        try {
            RoutineProcessBuilderFactory routineProcessBuilderFactory = new RoutineProcessBuilderFactory(
                    new LibraryServiceClientFactory().createClient(configuration.getLibraryEndpoint()),
                    configuration
            );

            // Create ReduceExecutorServices for all threads.
            LOGGER.info("Start {} ReducerExecutorServices.", configuration.getExecutionThreads());
            for (int i = 0; i < configuration.getExecutionThreads(); i++) {
                ReduceJobExecutorService executorService = new ReduceJobExecutorService(
                        this.reduceJobQueuePriorityWrapper,
                        this.resourceQueues,
                        routineProcessBuilderFactory,
                        this.storeRoutineId,
                        this
                );

                executorService.setName("ReduceExecutionThread " + i);
                executorService.setDaemon(true);
                executorService.start();
                reduceJobExecutorServices.add(executorService);
            }
        } catch (ClientCreationException e) {
            LOGGER.error("Error while creating LibraryServiceClient on startup.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected QueuePriorityWrapper getQueuePriorityWrapper() {
        return reduceJobQueuePriorityWrapper;
    }

    @Override
    protected List<? extends ExecutorService> getExecutorServices() {
        return reduceJobExecutorServices;
    }

    @Override
    protected Queue createQueueInstance(String qId) {
        return new ReduceJobQueue(qId);
    }

    @Override
    public List<String> getQueueIds() {
        return reduceJobQueuePriorityWrapper.getAllQueues()
                .stream()
                .map(Queue::getQueueId)
                .collect(Collectors.toList());
    }


    @Override
    protected void throwException(String eId, String message) throws Exception {
        throw new UnknownJobException(message);
    }

    @Override
    protected Set<ITuple<ContextIndicator, ?>> getLogContext(ReduceJobDTO element) {
        return new ContextSetBuilder()
                .add(ContextIndicator.PROGRAM_CONTEXT, element.getJob().getProgramId())
                .add(ContextIndicator.JOB_CONTEXT, element.getJobId())
                .build();
    }

    @Override
    protected Set<ITuple<ContextIndicator, ?>> getLogContext(String elementId) {
        return new ContextSetBuilder()
                .add(ContextIndicator.JOB_CONTEXT, elementId)
                .build();
    }

    @Override
    protected void removeElementFromQueues(String eId) {
        reduceJobQueuePriorityWrapper.getAllQueues().forEach(reduceJobQueue ->  reduceJobQueue.remove(eId));
    }

    @Override
    protected void setState(ReduceJobDTO element, ExecutionState state) {
        element.setState(state);
    }

    @Override
    protected List<String> getElementIds(List<ReduceJobDTO> elements) {
        return elements.stream().map(ReduceJobDTO::getJobId).collect(Collectors.toList());
    }

    @Override
    protected List<? extends Queue> getQueues() {
        return reduceJobQueuePriorityWrapper.getAllQueues();
    }

    @Override
    protected Future<Void> queueElements(String qId, List<ReduceJobDTO> elementsToQueue, ServiceEndpointDTO targetNodeEndpoint) throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    @Override
    protected void notifyObservers(String nId, List<String> eIds) {
        // do nothing
    }

    @Override
    protected void finishedExecutionOfElement(String eId) {
        activeReduceJobs.get(eId).countDown();
    }

    @Override
    protected String getElementName() {
        return ELEMENT_NAME;
    }

    public void reduceJob(String jId) throws UnknownJobException {
    	if (!resourceQueues.containsKey(jId)) {
            LOGGER.error(DEFLoggerFactory.createJobContext(jId), "ReduceJob does not exist.");
            throw new UnknownJobException(MessageFormat.format("ReduceJob with id {0} is not known.", jId));
        }

        try {
            ResourceDTO end = new ResourceDTO();
            end.setId(UUID.randomUUID().toString());
            end.setKey(KEY_REDUCE);
			end.setData(new byte[]{});
            resourceQueues.get(jId).queue(end);
        } catch (InterruptedException e) {
            LOGGER.error("Error while reducing job with id {}", jId);
        }

    }

    public void addResourcesToReduce(String jId, List<ResourceDTO> resources) throws UnknownJobException {
        if (!resourceQueues.containsKey(jId)) {
            LOGGER.error(DEFLoggerFactory.createJobContext(jId), "ReduceJob does not exist.", jId);
            throw new UnknownJobException(MessageFormat.format("ReduceJob with id {0} is not known.", jId));
        }

        try {
            Set<String> reduceKeys = new HashSet<>();
            ResourceQueue queue = resourceQueues.get(jId);
            for (ResourceDTO resource : resources) {
                queue.queue(resource);
                reduceKeys.add(resource.getKey());
            }
            notifyAllObservers(observerClient -> observerClient.notifyReduceKeysReceived(
                    getNodeConfiguration().getId(),
                    jId,
                    new ArrayList<>(reduceKeys)
            ));
        } catch (InterruptedException e) {
            LOGGER.error("Error while adding resources to reduce to job with id {}", jId);
        }
    }

    public void abortReduceJob(String jId) throws UnknownJobException {
        if (!resourceQueues.containsKey(jId)) {
            LOGGER.error(DEFLoggerFactory.createJobContext(jId), "ReduceJob does not exist.", jId);
            throw new UnknownJobException(MessageFormat.format("ReduceJob with id {0} is not known.", jId));
        }

        try {
            ReduceJobDTO reduceJob = elementCache.fetch(jId);
            reduceJob.addToMessages("Aborted by user.");
            ExecutionState oldState = reduceJob.getState();

            abortElement(jId, reduceJob, oldState);
            resourceQueues.get(jId).clear();
            resourceQueues.remove(jId);

            activeReduceJobs.get(jId).countDown();

        } catch (UnknownCacheObjectException e) {
            LOGGER.error("Queue with id {} does not exist.", jId, e);
            throw new UnknownJobException(MessageFormat.format("Job with id {0} is not known.", jId));
        } catch (IOException e) {
            LOGGER.error("Error while aborting reduce job.", e);
        }

    }

    public void createReduceJob(JobDTO job) throws UnknownProgramException {
        try {
            activeReduceJobs.put(job.getId(), new CountDownLatch(1));

            if (!reduceJobQueuePriorityWrapper.containsQueue(job.getProgramId())) {
                createQueue(job.getProgramId());
            }

            ResourceQueue resourceQueue = new ResourceQueue(job.getId(), DTO_RESOURCE_CACHE_CONTEXT);
            resourceQueue.release();
            resourceQueues.put(job.getId(), resourceQueue);

            ReduceJobDTO reduceJob = new ReduceJobDTO();
            reduceJob.setJobId(job.getId());
            reduceJob.setJob(job);
            reduceJob.setState(ExecutionState.SCHEDULED);
            queueElements(job.getProgramId(), Collections.singletonList(reduceJob));

        } catch (QueueNotExistsException e) {
            LOGGER.error("Queue with id {} does not exist.", job.getProgramId(), e);
            throw new UnknownProgramException(MessageFormat.format("Program with id {0} is not known.", job.getProgramId()));
        }
    }


    public List<ResourceDTO> fetchResults(String jId) throws Exception {
        if (!resourceQueues.containsKey(jId)) {
            LOGGER.error(DEFLoggerFactory.createJobContext(jId), "ReduceJob does not exist.", jId);
            throw new UnknownJobException(MessageFormat.format("ReduceJob with id {0} is not known.", jId));
        }

        activeReduceJobs.get(jId).await();
        ReduceJobDTO reduceJob = fetchFinishedElement(jId);
        return reduceJob.getJob().getReducedResults();
    }

    /**
     * Helper function for unit testing
     */
    protected DTOCache<ReduceJobDTO> getReduceJobCache() { return elementCache; }

    protected Map<String, CountDownLatch> getActiveReduceJobs() { return activeReduceJobs; }
}
