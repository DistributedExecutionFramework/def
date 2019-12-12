package at.enfilo.def.node.impl;

import at.enfilo.def.dto.cache.DTOCache;
import at.enfilo.def.node.queue.QueuePriorityWrapper;
import at.enfilo.def.node.routine.exec.SequenceStepsExecutor;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.transfer.dto.ExecutionState;
import org.apache.thrift.TBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ExecutorServiceTest {

    private ExecutorService executorService;
    private QueuePriorityWrapper queuePriorityWrapper;
    private IStateChangeListener stateChangeListener;
    private DTOCache cache;
    private String storeRoutineId;

    @Before
    public void setUp() throws Exception {

        stateChangeListener = Mockito.mock(IStateChangeListener.class);
        queuePriorityWrapper = Mockito.mock(QueuePriorityWrapper.class);
        cache = Mockito.mock(DTOCache.class);
        storeRoutineId = UUID.randomUUID().toString();

        executorService = new ExecutorService(
                queuePriorityWrapper,
                storeRoutineId,
                stateChangeListener,
                cache
        ) {
            @Override
            protected void logInfo(String message) {
                System.out.println(message);
            }

            @Override
            protected void logInfo(TBase element, String message) {
                System.out.println(message);
            }

            @Override
            protected void logError(String message, Exception e) {
                System.out.println(message);
                e.printStackTrace();
            }

            @Override
            protected void logError(TBase element, String message, Exception e) {
                System.out.println(message);
                e.printStackTrace();
            }

            @Override
            protected String getElementId(TBase element) {
                return null;
            }

            @Override
            protected ExecutionState getElementState(TBase element) {
                return null;
            }

            @Override
            protected SequenceStepsExecutor buildSequenceStepsExecutor(TBase element) {
                return null;
            }

            @Override
            protected void prepareElementForExecution(TBase element) {

            }

            @Override
            protected List<Result> executeElement(TBase element, SequenceStepsExecutor executor) throws Exception {
                return null;
            }

            @Override
            protected void handleSuccessfulExecutionOfElement(TBase element, List list) throws Exception {
                this.isActive = false;
            }

            @Override
            protected void handleFailedExecutionOfElement(TBase element, Exception e) {
                this.isActive = false;
            }
        };
    }

    @Test
    public void run() throws Exception {
        TBase element = Mockito.mock(TBase.class);
        WeakReference ref = Mockito.mock(WeakReference.class);

        when(ref.get()).thenReturn(element);
        when(queuePriorityWrapper.enqueue()).thenReturn(element);

        executorService.run();

        verify(queuePriorityWrapper, times(1)).enqueue();
        verify(cache, times(2)).cache(any(), any());
        verify(stateChangeListener, times(2)).notifyStateChanged(any(), any(), any());
    }
}
