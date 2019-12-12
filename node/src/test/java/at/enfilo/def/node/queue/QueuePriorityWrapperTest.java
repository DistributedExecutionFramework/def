package at.enfilo.def.node.queue;

import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.exception.QueueNotExistsException;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.dto.PeriodUnit;
import org.apache.thrift.TBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class QueuePriorityWrapperTest {

    private QueuePriorityWrapper queuePriorityWrapper;
    private NodeConfiguration configuration;
    private String queue1Id;
    private String queue2Id;
    private String queue3Id;
    private Queue queue1;
    private Queue queue2;
    private Queue queue3;
    private TBase element1;
    private TBase element2;
    private TBase element3;
    private IDEFLogger logger;

    @Before
    public void setUp() throws Exception {
        configuration = Mockito.mock(NodeConfiguration.class);
        logger = DEFLoggerFactory.getLogger(QueuePriorityWrapperTest.class);

        when(configuration.getQueueLifeTime()).thenReturn(2);
        when(configuration.getQueueLifeTimeUnit()).thenReturn(PeriodUnit.HOURS);

        queue1Id = UUID.randomUUID().toString();
        queue2Id = UUID.randomUUID().toString();
        queue3Id = UUID.randomUUID().toString();

        queue1 = Mockito.mock(Queue.class);
        queue2 = Mockito.mock(Queue.class);
        queue3 = Mockito.mock(Queue.class);

        element1 = Mockito.mock(TBase.class);
        element2 = Mockito.mock(TBase.class);
        element3 = Mockito.mock(TBase.class);

        when(queue1.getQueueId()).thenReturn(queue1Id);
        when(queue2.getQueueId()).thenReturn(queue2Id);
        when(queue3.getQueueId()).thenReturn(queue3Id);

        when(queue1.enqueue()).thenReturn(element1);
        when(queue2.enqueue()).thenReturn(element2);
        when(queue3.enqueue()).thenReturn(element3);

        when(queue1.isReleased()).thenReturn(true);
        when(queue2.isReleased()).thenReturn(true);
        when(queue3.isReleased()).thenReturn(true);

        when(queue1.size()).thenReturn(1);
        when(queue2.size()).thenReturn(1);
        when(queue3.size()).thenReturn(1);

        when(queue1.hasElements()).thenReturn(true);
        when(queue2.hasElements()).thenReturn(true);
        when(queue3.hasElements()).thenReturn(true);

        queuePriorityWrapper = new QueuePriorityWrapper(configuration);
    }

    @Test (timeout = 10000)
    public void enqueueSingle() throws Exception {
        queuePriorityWrapper.addQueue(queue1);
        queuePriorityWrapper.notifyQueueReleased(queue1);

        TBase element = queuePriorityWrapper.enqueue();
        assertEquals(element1, element);
    }

    @Test (timeout = 10000)
    public void enqueueMulti() throws Exception {
        queuePriorityWrapper.addQueue(queue1);
        queuePriorityWrapper.addQueue(queue2);
        queuePriorityWrapper.addQueue(queue3);

        assertEquals(element1, queuePriorityWrapper.enqueue());
        when(queue1.hasElements()).thenReturn(false);
        assertEquals(element2, queuePriorityWrapper.enqueue());
        when(queue2.hasElements()).thenReturn(false);
        assertEquals(element3, queuePriorityWrapper.enqueue());
    }

    @Test (timeout = 10000)
    public void releaseAndPauseQueue() throws Exception {
        queuePriorityWrapper.addQueue(queue1);
        queuePriorityWrapper.addQueue(queue2);
        queuePriorityWrapper.addQueue(queue3);

        queuePriorityWrapper.notifyQueueReleased(queue1);
        queuePriorityWrapper.notifyQueueReleased(queue2);
        queuePriorityWrapper.notifyQueueReleased(queue3);

        // Pause first queue, next element should be element2
        when(queue1.isReleased()).thenReturn(false);
        Thread.sleep(100);
        assertEquals(element2, queuePriorityWrapper.enqueue());

        // Release first queue, next element should be element1
        when(queue1.isReleased()).thenReturn(true);
        Thread.sleep(100);
        assertEquals(element1, queuePriorityWrapper.enqueue());
    }

    @Test (timeout = 10000)
    public void addAndGetQueue() throws Exception {
        assertFalse(queuePriorityWrapper.containsQueue(queue1.getQueueId()));
        assertFalse(queuePriorityWrapper.containsQueue(queue2.getQueueId()));
        assertFalse(queuePriorityWrapper.containsQueue(queue3.getQueueId()));
        assertEquals(0, queuePriorityWrapper.getNumberOfQueues());

        queuePriorityWrapper.addQueue(queue1);
        queuePriorityWrapper.addQueue(queue2);
        queuePriorityWrapper.addQueue(queue3);
        queuePriorityWrapper.addQueue(queue3);

        assertTrue(queuePriorityWrapper.containsQueue(queue1.getQueueId()));
        assertTrue(queuePriorityWrapper.containsQueue(queue2.getQueueId()));
        assertTrue(queuePriorityWrapper.containsQueue(queue3.getQueueId()));
        assertEquals(3, queuePriorityWrapper.getNumberOfQueues());

        assertEquals(queue1, queuePriorityWrapper.getQueue(queue1.getQueueId()));
        assertEquals(queue2, queuePriorityWrapper.getQueue(queue2.getQueueId()));
        assertEquals(queue3, queuePriorityWrapper.getQueue(queue3.getQueueId()));
    }

    @Test (expected = QueueNotExistsException.class)
    public void getQueue_unknownQueue() throws Exception {
        queuePriorityWrapper.getQueue(UUID.randomUUID().toString());
    }

    @Test
    public void getNumberOfQueues() throws Exception {
        assertEquals(0, queuePriorityWrapper.getNumberOfQueues());

        queuePriorityWrapper.addQueue(queue1);

        assertEquals(1, queuePriorityWrapper.getNumberOfQueues());
    }

    @Test
    public void containsQueue() throws Exception {
        assertFalse(queuePriorityWrapper.containsQueue(queue1.getQueueId()));

        queuePriorityWrapper.addQueue(queue1);

        assertTrue(queuePriorityWrapper.containsQueue(queue1.getQueueId()));
    }

    @Test (timeout = 10000)
    public void deleteQueue() throws Exception {
        queuePriorityWrapper.addQueue(queue1);
        queuePriorityWrapper.addQueue(queue2);

        queuePriorityWrapper.notifyQueueReleased(queue1);
        queuePriorityWrapper.notifyQueueReleased(queue2);

        // Delete first two queues, only third queue should be available
        queuePriorityWrapper.deleteQueue(queue1.getQueueId());

        assertEquals(1, queuePriorityWrapper.getAllQueues().size());
        assertFalse(queuePriorityWrapper.getAllQueues().contains(queue1));
        assertTrue(queuePriorityWrapper.getAllQueues().contains(queue2));
    }


    @Test (expected = QueueNotExistsException.class)
    public void deleteQueue_unknownQueue() throws Exception {
        queuePriorityWrapper.deleteQueue(UUID.randomUUID().toString());
    }

    @Test
    public void getAllQueues() throws Exception {
        assertEquals(0, queuePriorityWrapper.getAllQueues().size());

        queuePriorityWrapper.addQueue(queue1);
        queuePriorityWrapper.addQueue(queue2);
        queuePriorityWrapper.addQueue(queue3);

        assertEquals(3, queuePriorityWrapper.getAllQueues().size());
        assertTrue(queuePriorityWrapper.getAllQueues().contains(queue1));
        assertTrue(queuePriorityWrapper.getAllQueues().contains(queue2));
        assertTrue(queuePriorityWrapper.getAllQueues().contains(queue3));
    }
}
