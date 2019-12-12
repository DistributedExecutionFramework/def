package at.enfilo.def.reducer.impl;

import at.enfilo.def.datatype.DEFDouble;
import at.enfilo.def.demo.DoubleSumReducer;
import at.enfilo.def.demo.MemoryStorer;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.util.BaseRoutineRegistry;
import at.enfilo.def.node.api.exception.QueueNotExistsException;
import at.enfilo.def.node.impl.IStateChangeListener;
import at.enfilo.def.node.impl.NodeServiceController;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.queue.ResourceQueue;
import at.enfilo.def.node.routine.exec.SequenceStepsExecutor;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.reducer.queue.ReduceJobQueue;
import at.enfilo.def.reducer.util.ReducerConfiguration;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.dto.*;
import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

public class ReduceJobExecutorServiceTest {

    private static String DTO_CACHE_CONTEXT_REDUCEJOB = "test-reducejobs";

    private ReduceJobExecutorService executorService;
    private ILibraryServiceClient libraryServiceClient;
    private IStateChangeListener stateChangeListener;
    private TDeserializer deserializer;
    private DTOCache<ReduceJobDTO> reduceJobCache;
    private DTOCache<ResourceDTO> resourcesCache;
    private Map<String, ResourceQueue> resourceQueues;
    private QueuePriorityWrapper<ReduceJobDTO> reduceJobQueuePriorityWrapper;
    private Thread executorServiceThread;
    private TSerializer serializer;

    @Before
    public void setUp() throws Exception {
        serializer = new TSerializer();

        libraryServiceClient = Mockito.mock(ILibraryServiceClient.class);
        stateChangeListener = Mockito.mock(IStateChangeListener.class);
        //cache = Mockito.mock(DTOCache.class);
        reduceJobCache = DTOCache.getInstance(DTO_CACHE_CONTEXT_REDUCEJOB, ReduceJobDTO.class);
        resourcesCache = DTOCache.getInstance(NodeServiceController.DTO_RESOURCE_CACHE_CONTEXT, ResourceDTO.class);

        RoutineProcessBuilderFactory routineProcessBuilderFactory = new RoutineProcessBuilderFactory(
                libraryServiceClient,
                ReducerConfiguration.getDefault()
        );

        ReducerConfiguration configuration = Mockito.mock(ReducerConfiguration.class);
        when(configuration.getWorkingDir()).thenReturn("/tmp/def/");
        when(configuration.getQueueLifeTime()).thenReturn(30);
        when(configuration.getQueueLifeTimeUnit()).thenReturn(PeriodUnit.MINUTES);

        deserializer = Mockito.mock(TDeserializer.class);
        when(deserializer.partialDeserializeString(any(), any())).thenReturn(UUID.randomUUID().toString());

        reduceJobQueuePriorityWrapper = new QueuePriorityWrapper<>(NodeConfiguration.getDefault());
        resourceQueues = new HashMap<>();

        executorService = new ReduceJobExecutorService(
                reduceJobQueuePriorityWrapper,
                resourceQueues,
                routineProcessBuilderFactory,
                null,
                stateChangeListener,
                configuration,
                reduceJobCache,
                deserializer
        );
    }

    @Test
    public void getElementId() {
        String jId = UUID.randomUUID().toString();
        ReduceJobDTO job = new ReduceJobDTO();
        job.setJobId(jId);

        assertEquals(jId, executorService.getElementId(job));
    }

    @Test
    public void getElementState() {
        ExecutionState state = ExecutionState.SUCCESS;
        ReduceJobDTO job = new ReduceJobDTO();
        job.setState(state);

        assertEquals(state, executorService.getElementState(job));
    }

    @Test
    public void buildSequenceStepsExecutor() throws Exception {
        String jId = UUID.randomUUID().toString();
        ReduceJobDTO reduceJob = Mockito.mock(ReduceJobDTO.class);
        JobDTO job = Mockito.mock(JobDTO.class);
        when(reduceJob.getJobId()).thenReturn(jId);
        when(reduceJob.getJob()).thenReturn(job);
        when(job.getReduceRoutineId()).thenReturn(UUID.randomUUID().toString());
        String storeRoutineId = UUID.randomUUID().toString();
        executorService.setStoreRoutineId(storeRoutineId);

        ResourceQueue queue = Mockito.mock(ResourceQueue.class);
        when(queue.getQueueId()).thenReturn(jId);
        resourceQueues.put(queue.getQueueId(), queue);

        SequenceStepsExecutor executor = executorService.buildSequenceStepsExecutor(reduceJob);

        assertNotNull(executor);

        verify(reduceJob, atLeast(1)).getJobId();
        verify(reduceJob, atLeast(1)).getJob();
        verify(job, atLeast(1)).getReduceRoutineId();
        assertEquals(2, executor.getNumberOfSequenceSteps());
        verify(queue).registerObserver(executor.getCommunicator());
    }

    @Test (expected = QueueNotExistsException.class)
    public void buildSequenceStepsExecutor_unknownJob() throws Exception {
        String jId = UUID.randomUUID().toString();
        ReduceJobDTO reduceJob = Mockito.mock(ReduceJobDTO.class);
        JobDTO job = Mockito.mock(JobDTO.class);
        when(reduceJob.getJobId()).thenReturn(jId);
        when(reduceJob.getJob()).thenReturn(job);
        when(job.getReduceRoutineId()).thenReturn(UUID.randomUUID().toString());
        String storeRoutineId = UUID.randomUUID().toString();
        executorService.setStoreRoutineId(storeRoutineId);

        executorService.buildSequenceStepsExecutor(reduceJob);
    }

    @Test
    public void prepareElementForExecution() {
        ReduceJobDTO job = Mockito.mock(ReduceJobDTO.class);

        executorService.prepareElementForExecution(job);

        verify(job).setState(ExecutionState.RUN);
        verify(job).setStartTime(anyLong());
    }

    @Test
    public void executeElement() throws Exception {
        ReduceJobDTO job = new ReduceJobDTO();
        SequenceStepsExecutor executor = Mockito.mock(SequenceStepsExecutor.class);

        executorService.executeElement(job, executor);

        verify(executor, times(1)).run(any(), any());
    }

    @Test
    public void handleSuccessfulExecutionOfElement() throws Exception {
        ReduceJobDTO reduceJob = Mockito.mock(ReduceJobDTO.class);
        JobDTO job = Mockito.mock(JobDTO.class);
        when(reduceJob.getJob()).thenReturn(job);
        Result result1 = Mockito.mock(Result.class);
        Result result2 = Mockito.mock(Result.class);

        executorService.handleSuccessfulExecutionOfElement(reduceJob, Arrays.asList(result1, result2));

        verify(job).setReducedResults(any());
        verify(reduceJob).setState(ExecutionState.SUCCESS);
    }

    @Test
    public void handleFailedExecutionOfElement() {
        ReduceJobDTO job = Mockito.mock(ReduceJobDTO.class);
        Exception exception = Mockito.mock(Exception.class);

        executorService.handleFailedExecutionOfElement(job, exception);

        verify(job).setState(ExecutionState.FAILED);
        verify(job).addToMessages(any());
        verify(exception).getMessage();
    }

    @Test
    public void extractResults() throws Exception {
        Result result1 = Mockito.mock(Result.class);
        Result result2 = Mockito.mock(Result.class);
        List<Result> results = Arrays.asList(result1, result2);

        List<ResourceDTO> resources = executorService.extractResults(results);

        verify(deserializer, atLeast(2)).partialDeserializeString(any(), any());
        assertNotNull(resources);
        assertEquals(results.size(), resources.size());
    }

    @Test
    public void runReduceJob() throws Exception {
        BaseRoutineRegistry registry = BaseRoutineRegistry.getInstance();
        RoutineDTO doubleSumReducer = registry.get(UUID.nameUUIDFromBytes(DoubleSumReducer.class.getCanonicalName().getBytes()).toString());
        for (RoutineBinaryDTO binary : doubleSumReducer.getRoutineBinaries()) {
            binary.setExecutionUrl(binary.getUrl());
        }
        RoutineDTO memoryStorer = registry.get(UUID.nameUUIDFromBytes(MemoryStorer.class.getCanonicalName().getBytes()).toString());
        for (RoutineBinaryDTO binary : memoryStorer.getRoutineBinaries()) {
            binary.setExecutionUrl(binary.getUrl());
        }

        // Mock library
        Future<RoutineDTO> doubleSumReducerFuture = Mockito.mock(Future.class);
        when(doubleSumReducerFuture.isDone()).thenReturn(true);
        when(doubleSumReducerFuture.get()).thenReturn(doubleSumReducer);
        when(libraryServiceClient.getRoutine(doubleSumReducer.getId())).thenReturn(doubleSumReducerFuture);
        Future<RoutineDTO> memoryStorerFuture = Mockito.mock(Future.class);
        when(memoryStorerFuture.isDone()).thenReturn(true);
        when(memoryStorerFuture.get()).thenReturn(memoryStorer);
        when(libraryServiceClient.getRoutine(memoryStorer.getId())).thenReturn(memoryStorerFuture);

        executorService.setStoreRoutineId(memoryStorer.getId());

        String pId = UUID.randomUUID().toString();

        ReduceJobDTO reduceJob = new ReduceJobDTO();
        JobDTO job = new JobDTO();
        String jId = UUID.randomUUID().toString();
        job.setId(jId);
        job.setProgramId(pId);
        job.setState(ExecutionState.SUCCESS);
        job.setReduceRoutineId(doubleSumReducer.getId());
        reduceJob.setJobId(jId);
        reduceJob.setJob(job);
        reduceJob.setState(ExecutionState.SCHEDULED);

        DEFDouble value1 = new DEFDouble(2.5);
        ResourceDTO resource1 = prepareResource(UUID.randomUUID().toString(), value1.get_id(), value1);
        DEFDouble value2 = new DEFDouble(5.4);
        ResourceDTO resource2 = prepareResource(UUID.randomUUID().toString(), value2.get_id(), value2);

        resourcesCache.cache(resource1.getId(), resource1);
        resourcesCache.cache(resource2.getId(), resource2);

        ReduceJobQueue reduceJobQueue = new ReduceJobQueue(pId);
        reduceJobQueue.release();
        reduceJobQueuePriorityWrapper.addQueue(reduceJobQueue);

        ResourceQueue resourceQueue = new ResourceQueue(jId, NodeServiceController.DTO_RESOURCE_CACHE_CONTEXT);
        resourceQueue.release();
        resourceQueues.put(resourceQueue.getQueueId(), resourceQueue);

        executorServiceThread = new Thread(executorService);
        executorServiceThread.start();
        await().atMost(10, TimeUnit.SECONDS).until(executorService::isActive);

        reduceJobQueuePriorityWrapper.getQueue(pId).queue(reduceJob);

        await().atMost(10, TimeUnit.SECONDS).until(executorService::isRunning);

        resourceQueues.get(jId).queue(resource1);
        resourceQueues.get(jId).queue(resource2);

        ResourceDTO end = new ResourceDTO();
        end.setId(UUID.randomUUID().toString());
        end.setKey("REDUCE");
        end.setData(new byte[]{});
        resourceQueues.get(jId).queue(end);

        await().atMost(30, TimeUnit.SECONDS).until(this::executorServiceNotRunning);

        ReduceJobDTO reducedJob = reduceJobCache.fetch(jId);

        assertEquals(ExecutionState.SUCCESS, reducedJob.getState());
        assertTrue(reducedJob.getJob().getReducedResultsSize() == 1);
    }

    private boolean executorServiceNotRunning() {
        return !executorService.isRunning();
    }

    private ResourceDTO prepareResource(String rId, String dataTypeId, TBase value) throws TException {
        ResourceDTO resource = new ResourceDTO(rId, dataTypeId);
        if (value != null) {
            resource.setData(serializer.serialize(value));
        }
        return resource;
    }
}
