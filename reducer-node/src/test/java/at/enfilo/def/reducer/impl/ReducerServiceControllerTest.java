package at.enfilo.def.reducer.impl;

import at.enfilo.def.node.observer.api.client.INodeObserverServiceClient;
import at.enfilo.def.node.observer.api.client.factory.NodeObserverServiceClientFactory;
import at.enfilo.def.node.queue.Queue;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.queue.ResourceQueue;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.reducer.api.ReducerServiceClientFactory;
import at.enfilo.def.reducer.queue.ReduceJobQueue;
import at.enfilo.def.reducer.server.Reducer;
import at.enfilo.def.reducer.util.ReducerConfiguration;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.JobDTO;
import at.enfilo.def.transfer.dto.ReduceJobDTO;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

public class ReducerServiceControllerTest {

    private static String DTO_CACHE_CONTEXT = "reducer-service-controller-test";

    private ReducerServiceController controller;
    private QueuePriorityWrapper<ReduceJobDTO> reduceJobQueuePriorityWrapper;
    private Map<String, ResourceQueue> resourceQueues;
    private List<INodeObserverServiceClient> observers;
    private ReducerServiceClientFactory reducerServiceClientFactory;
    private Set<String> finishedReduceJobs;
    private NodeObserverServiceClientFactory nodeObserverServiceClientFactory;

    @Before
    public void setUp() throws Exception {
        reduceJobQueuePriorityWrapper = new QueuePriorityWrapper<>(NodeConfiguration.getDefault());
        resourceQueues = new HashMap<>();
        finishedReduceJobs = new HashSet<>();
        observers = new LinkedList<>();
        reducerServiceClientFactory = Mockito.mock(ReducerServiceClientFactory.class);
        nodeObserverServiceClientFactory = Mockito.mock(NodeObserverServiceClientFactory.class);

        Constructor<ReducerServiceController> constructor = ReducerServiceController.class.getDeclaredConstructor(
                QueuePriorityWrapper.class,
                Map.class,
                Set.class,
                List.class,
                ReducerConfiguration.class,
                NodeObserverServiceClientFactory.class
        );
        constructor.setAccessible(true);
        controller = constructor.newInstance(
                reduceJobQueuePriorityWrapper,
                resourceQueues,
                finishedReduceJobs,
                observers,
                Reducer.getInstance().getConfiguration(),
                nodeObserverServiceClientFactory
        );
    }

    @Test
    public void getQueueIds() {
        String qId = UUID.randomUUID().toString();
        ReduceJobQueue queue = new ReduceJobQueue(qId);
        reduceJobQueuePriorityWrapper.addQueue(queue);

        List<String> qIds = controller.getQueueIds();

        assertEquals(1, qIds.size());
        assertTrue(qIds.contains(qId));
    }

    @Test
    public void removeElementFromQueues() throws Exception {
        String q1Id = UUID.randomUUID().toString();
        String q2Id = UUID.randomUUID().toString();

        ReduceJobQueue queue1 = new ReduceJobQueue(q1Id);
        ReduceJobQueue queue2 = new ReduceJobQueue(q2Id);

        String j1Id = UUID.randomUUID().toString();
        String j2Id = UUID.randomUUID().toString();
        String j3Id = UUID.randomUUID().toString();

        ReduceJobDTO job1 = new ReduceJobDTO();
        job1.setJobId(j1Id);
        ReduceJobDTO job2 = new ReduceJobDTO();
        job2.setJobId(j2Id);
        ReduceJobDTO job3 = new ReduceJobDTO();
        job3.setJobId(j3Id);

        queue1.queue(job1);
        queue1.queue(job2);
        queue2.queue(job3);

        reduceJobQueuePriorityWrapper.addQueue(queue1);
        reduceJobQueuePriorityWrapper.addQueue(queue2);

        assertEquals(2, reduceJobQueuePriorityWrapper.getQueue(q1Id).size());
        assertEquals(1, reduceJobQueuePriorityWrapper.getQueue(q2Id).size());

        controller.removeElementFromQueues(j1Id);

        assertEquals(1, reduceJobQueuePriorityWrapper.getQueue(q1Id).size());
        assertEquals(1, reduceJobQueuePriorityWrapper.getQueue(q2Id).size());
    }

    @Test
    public void setState() {
        ExecutionState state = ExecutionState.SUCCESS;
        ReduceJobDTO reduceJob = new ReduceJobDTO();

        controller.setState(reduceJob, state);

        assertEquals(state, reduceJob.getState());
    }

    @Test
    public void getElementIds() {
        String j1Id = UUID.randomUUID().toString();
        String j2Id = UUID.randomUUID().toString();
        String j3Id = UUID.randomUUID().toString();

        ReduceJobDTO job1 = new ReduceJobDTO();
        job1.setJobId(j1Id);
        ReduceJobDTO job2 = new ReduceJobDTO();
        job2.setJobId(j2Id);
        ReduceJobDTO job3 = new ReduceJobDTO();
        job3.setJobId(j3Id);

        List<String> elementIds = controller.getElementIds(Arrays.asList(job1, job2, job3));

        assertEquals(3, elementIds.size());
        assertTrue(elementIds.contains(j1Id));
        assertTrue(elementIds.contains(j2Id));
        assertTrue(elementIds.contains(j3Id));
    }

    @Test
    public void getQueues() {
        ReduceJobQueue queue1 = new ReduceJobQueue(UUID.randomUUID().toString());
        ReduceJobQueue queue2 = new ReduceJobQueue(UUID.randomUUID().toString());
        ReduceJobQueue queue3 = new ReduceJobQueue(UUID.randomUUID().toString());

        reduceJobQueuePriorityWrapper.addQueue(queue1);
        reduceJobQueuePriorityWrapper.addQueue(queue2);
        reduceJobQueuePriorityWrapper.addQueue(queue3);

        List<? extends Queue> queues = controller.getQueues();

        assertEquals(3, queues.size());
        assertTrue(queues.contains(queue1));
        assertTrue(queues.contains(queue2));
        assertTrue(queues.contains(queue3));
    }

    @Test
    public void reduceJob() throws Exception {
        String jId = UUID.randomUUID().toString();
        ResourceQueue queue = new ResourceQueue(jId, DTO_CACHE_CONTEXT);
        queue.release();

        resourceQueues.put(queue.getQueueId(), queue);

        assertEquals(0, queue.size());

        controller.reduceJob(jId);

        assertEquals(1, queue.size());
        ResourceDTO fetchedResource = queue.enqueue();
        assertEquals(ReducerServiceController.KEY_REDUCE, fetchedResource.getKey());
        assertNotNull(fetchedResource.getData());
    }

    @Test (expected = UnknownJobException.class)
    public void reduceJob_unknownJob() throws Exception {
        controller.reduceJob(UUID.randomUUID().toString());
    }

    @Test
    public void addResourcesToReduce() throws Exception {
        String jId = UUID.randomUUID().toString();
        ResourceQueue queue = new ResourceQueue(jId, DTO_CACHE_CONTEXT);
        queue.release();

        resourceQueues.put(queue.getQueueId(), queue);

        ResourceDTO resource1 = new ResourceDTO();
        resource1.setId(UUID.randomUUID().toString());
        ResourceDTO resource2 = new ResourceDTO();
        resource2.setId(UUID.randomUUID().toString());

        assertEquals(0, queue.size());

        controller.addResourcesToReduce(jId, Arrays.asList(resource1, resource2));

        assertEquals(2, queue.size());
        ResourceDTO firstEnqueuedResource = queue.enqueue();
        ResourceDTO secondEnqueuedResource = queue.enqueue();
        assertEquals(resource1, firstEnqueuedResource);
        assertEquals(resource2, secondEnqueuedResource);
    }

    @Test (expected = UnknownJobException.class)
    public void addResourcesToReduce_unknownJob() throws Exception {
        controller.addResourcesToReduce(UUID.randomUUID().toString(), Collections.emptyList());
    }

    @Test
    public void abortReduceJob() throws Exception {
        String jId = UUID.randomUUID().toString();
        ReduceJobDTO reduceJob = new ReduceJobDTO();
        reduceJob.setJobId(jId);
        reduceJob.setState(ExecutionState.RUN);
        JobDTO job = new JobDTO();
        job.setId(jId);
        job.setProgramId(UUID.randomUUID().toString());
        reduceJob.setJob(job);
        controller.getReduceJobCache().cache(jId, reduceJob);
        controller.getActiveReduceJobs().put(jId, new CountDownLatch(1));

        ResourceQueue queue = new ResourceQueue(jId, DTO_CACHE_CONTEXT);
        resourceQueues.put(queue.getQueueId(), queue);

        assertTrue(resourceQueues.containsKey(jId));

        controller.abortReduceJob(jId);

        assertTrue(reduceJob.getMessages().size() > 0);
        assertFalse(resourceQueues.containsKey(jId));
    }

    @Test (expected = UnknownJobException.class)
    public void abortReduceJob_unknownJob() throws Exception {
        controller.abortReduceJob(UUID.randomUUID().toString());
    }

    @Test
    public void createReduceJob() throws Exception {
        String pId = UUID.randomUUID().toString();
        String jId = UUID.randomUUID().toString();
        JobDTO job = new JobDTO();
        job.setId(jId);
        job.setProgramId(pId);

        assertEquals(0, resourceQueues.size());
        assertEquals(0, reduceJobQueuePriorityWrapper.getNumberOfQueues());

        controller.createReduceJob(job);

        assertEquals(1, reduceJobQueuePriorityWrapper.getNumberOfQueues());
        assertEquals(1, resourceQueues.size());
        assertTrue(resourceQueues.containsKey(jId));
    }

    @Test (expected = UnknownJobException.class)
    public void fetchFinishedJob_unknownReduceJob() throws Exception {
        controller.fetchResults(UUID.randomUUID().toString());
    }
}
