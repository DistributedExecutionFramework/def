package at.enfilo.def.node.queue;

import at.enfilo.def.common.api.ITuple;
import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.logging.api.ContextIndicator;
import at.enfilo.def.node.api.exception.QueueNotReleasedException;
import at.enfilo.def.transfer.dto.ResourceDTO;
import org.apache.thrift.TBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class QueueTest {

    private Queue<ResourceDTO> queue;
    private LinkedBlockingDeque backgroundQueue;
    private IQueueObserver observer;
    private DTOCache cache;
    private ResourceDTO element;
    private String elementId;

    @Before
    public void setUp() throws Exception {

        backgroundQueue = Mockito.mock(LinkedBlockingDeque.class);
        observer = Mockito.mock(IQueueObserver.class);
        cache = Mockito.mock(DTOCache.class);
        element = new ResourceDTO();
        elementId = UUID.randomUUID().toString();
        element.setId(elementId);

        List<IQueueObserver> observers = new LinkedList<>();
        observers.add(observer);

        queue = new Queue<ResourceDTO>(
                UUID.randomUUID().toString(),
                backgroundQueue,
                cache
        ) {
            @Override
            protected String getElementId(ResourceDTO element) {
                return element.getId();
            }

            @Override
            protected Set<ITuple<ContextIndicator, ?>> getLoggingContext(String eId) {
                return null;
            }
        };
        queue.registerObserver(observer);
    }

    @Test
    public void release() throws Exception {
        queue.setReleased(false);
        queue.release();

        assertTrue(queue.isReleased());
        verify(observer, times(1)).notifyQueueReleased(queue);
    }

    @Test
    public void pause() throws Exception {
        queue.setReleased(true);
        queue.pause();

        assertEquals(false, queue.isReleased());
    }

    @Test
    public void enqueue_queueReleased() throws Exception {
        when(backgroundQueue.take()).thenReturn(elementId);
        when(cache.fetch(elementId)).thenReturn(element);
        queue.setReleased(true);

        TBase e = queue.enqueue();

        assertEquals(element, e);
        verify(backgroundQueue, times(1)).take();
        verify(cache, times(1)).fetch(any());
    }

    @Test (expected = QueueNotReleasedException.class)
    public void enqueue_queueNotReleased() throws Exception {
        queue.setReleased(false);
        queue.enqueue();
    }

    @Test
    public void queue() throws Exception {
        queue.queue(element);

        verify(cache, times(1)).cache(anyString(), any());
        verify(backgroundQueue, times(1)).put(any());
    }

    @Test
    public void clear() throws Exception {
        queue.setReleased(true);

        queue.clear();
        assertEquals(false, queue.isReleased());
        verify(backgroundQueue, times(1)).clear();
    }

    @Test
    public void remove_notContained() throws Exception {
        Iterator<String> iterator = Mockito.mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(false);
        when(backgroundQueue.iterator()).thenReturn(iterator);

        queue.remove(elementId);

        verify(backgroundQueue, times(1)).iterator();
        verify(iterator, times(1)).hasNext();
        verify(iterator, times(0)).next();
        verify(iterator, times(0)).remove();
        verify(cache, times(0)).fetch(any());
        verify(cache, times(0)).remove(anyString());
    }

    @Test
    public void remove_contained() throws Exception {
        Iterator<String> iterator = Mockito.mock(Iterator.class);
        when(iterator.hasNext()).thenReturn(true);
        when(iterator.next()).thenReturn(elementId);
        when(backgroundQueue.iterator()).thenReturn(iterator);

        queue.remove(elementId);

        verify(backgroundQueue, times(1)).iterator();
        verify(iterator, times(1)).hasNext();
        verify(iterator, times(1)).hasNext();
        verify(iterator, times(1)).next();
        verify(iterator, times(1)).remove();
        verify(cache, times(1)).fetch(any());
        verify(cache, times(1)).remove(anyString());
    }

    @Test
    public void registerObserver() throws Exception {
        int nrOfObservers = queue.getObservers().size();
        IQueueObserver newObserver = Mockito.mock(IQueueObserver.class);

        queue.registerObserver(newObserver);
        assertEquals(nrOfObservers + 1, queue.getObservers().size());
        assertTrue(queue.getObservers().contains(newObserver));
    }

    @After
    public void tearDown() throws Exception {
        queue = null;
    }
}
